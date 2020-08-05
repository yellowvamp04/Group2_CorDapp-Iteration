package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import main.java.com.template.contracts.ToDoContract;
import main.java.com.template.states.ToDoState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.Date;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class Initiator extends FlowLogic<SignedTransaction> {
    private final ProgressTracker progressTracker = new ProgressTracker();
    private final Party assignedTo;
    private final String taskDescription;

    public Initiator(Party assignedTo, String taskDescription) {
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
    }
    
    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Initiator flow logic goes here.
        ServiceHub serviceHub = getServiceHub();
        Party me = getOurIdentity();

        ToDoState ts = new ToDoState(me, assignedTo, taskDescription, new UniqueIdentifier(), new Date());
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);

        TransactionBuilder tb = new TransactionBuilder(notary);
        tb = tb.addOutputState(ts);
        tb = tb.addCommand(new ToDoContract.Commands.Create(), ImmutableList.of(ts.getAssignedBy().getOwningKey(), ts.getAssignedTo().getOwningKey()));

        SignedTransaction ptx = serviceHub.signInitialTransaction(tb);

        FlowSession assignedToSession = initiateFlow(assignedTo);

        SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, ImmutableSet.of(assignedToSession)));

        return subFlow(new FinalityFlow(stx, Arrays.asList(assignedToSession)));
    }
}
