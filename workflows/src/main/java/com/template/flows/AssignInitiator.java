package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.template.contracts.ToDoContract;
import com.template.states.ToDoState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.IdentityService;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.time.Duration;
import java.util.*;


// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class AssignInitiator extends FlowLogic<String> {
    private final ProgressTracker progressTracker = new ProgressTracker();
    private final String linearId;
    private final String assignedTo;
    private final Integer sleepSeconds;

    public AssignInitiator(String linearId, String assignedTo, Integer sleepSeconds) {
        this.linearId = linearId.trim();
        this.assignedTo = assignedTo.trim();
        this.sleepSeconds = sleepSeconds;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.
        ServiceHub serviceHub = getServiceHub();
        IdentityService identityService = serviceHub.getIdentityService();
        Set<Party> partySet = identityService.partiesFromName(assignedTo, true);

        if (partySet.isEmpty()) { return ("Sorry, no Party " + assignedTo + " is found"); }

        Party me = getOurIdentity();
        Party receiver = partySet.iterator().next();

        final QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(UUID.fromString(linearId)));
        final Vault.Page<ToDoState> taskStatePage = serviceHub.getVaultService().queryBy(ToDoState.class, queryCriteria);
        final List<StateAndRef<ToDoState>> states = taskStatePage.getStates();
        final StateAndRef<ToDoState> stateAndRef = states.get(0);
        final ToDoState ts = stateAndRef.getState().getData();
        final ToDoState ts2 = new ToDoState(me, receiver, ts.getTaskDescription(), ts.getDateCreation(), ts.getDeadline(), ts.getCompletionStatus());

        System.out.println(queryCriteria);
        System.out.println(taskStatePage);
        System.out.println(states);
        System.out.println(stateAndRef);
        System.out.println(ts);
        System.out.println("Task Description: " + ts.getTaskDescription());

        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);

        TransactionBuilder tb = new TransactionBuilder(notary);
        tb = tb.addInputState(stateAndRef);
        tb = tb.addOutputState(ts2);
        tb = tb.addCommand(new ToDoContract.Commands.Assign(), ImmutableList.of(me.getOwningKey(), receiver.getOwningKey()));
        tb = tb.setTimeWindow(getServiceHub().getClock().instant(), Duration.ofSeconds(sleepSeconds));
        tb.verify(serviceHub);

        final SignedTransaction ptx = serviceHub.signInitialTransaction(tb);

        FlowSession otherPartySession = initiateFlow(receiver);
        SignedTransaction ftx = subFlow(new CollectSignaturesFlow(ptx, ImmutableSet.of(otherPartySession), CollectSignaturesFlow.Companion.tracker()));

        SignedTransaction sftx = subFlow(new FinalityFlow(ftx, ImmutableSet.of(otherPartySession)));
        System.out.println(sftx);

        return ("ToDo assign request sent to party: " + receiver.getName());
    }
}