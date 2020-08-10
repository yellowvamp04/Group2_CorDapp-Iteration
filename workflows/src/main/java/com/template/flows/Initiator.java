package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.template.contracts.ToDoContract;
import com.template.states.ToDoState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;


// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class Initiator extends FlowLogic<SignedTransaction> {
    private final ProgressTracker progressTracker = new ProgressTracker();
    private final String taskDescription;
    private final String deadline;

    public Initiator(@Nullable String taskDescription, String deadline) {
        if (taskDescription.isEmpty()) { this.taskDescription = " "; }
        else { this.taskDescription = taskDescription; }
        this.deadline = deadline;
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

        ToDoState ts = new ToDoState(me, me, taskDescription, new Date(), deadline, "OPEN");

        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);

        System.out.println("ToDoState: " + ts);
        System.out.println("AssignedBy: " + ts.getAssignedBy());
        System.out.println("AssignedTo: " + ts.getAssignedTo());
        System.out.println("LinearId: " + ts.getLinearId());
        System.out.println("DateCreation: " + ts.getDateCreation());

        TransactionBuilder tb = new TransactionBuilder(notary);
        tb = tb.addOutputState(ts);
        tb = tb.addCommand(new ToDoContract.Commands.Create(), ImmutableList.of(me.getOwningKey(), me.getOwningKey()));

        tb.verify(serviceHub);

        final SignedTransaction ptx = serviceHub.signInitialTransaction(tb);

        return subFlow(new FinalityFlow(ptx, Arrays.asList()));
    }
}