package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.ToDoState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.utilities.ProgressTracker;
import java.util.*;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class ToDoDistInitiator extends FlowLogic<String> {
    private final ProgressTracker progressTracker = new ProgressTracker();

    public ToDoDistInitiator() {
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        ServiceHub serviceHub = getServiceHub();
        final Vault.Page<ToDoState> taskStatePage = serviceHub.getVaultService().queryBy(ToDoState.class);
        final List<StateAndRef<ToDoState>> states = taskStatePage.getStates();
        final StateAndRef<ToDoState> stateAndRef = states.get(0);
        final ToDoState ts = stateAndRef.getState().getData();
        ArrayList<String> taskDescription = new ArrayList<>();
        //System.out.println(queryCriteria);
        System.out.println(taskStatePage);
        System.out.println(states);
        System.out.println(stateAndRef);
        System.out.println(ts);
        //System.out.println("Task Description: " + ts.getTaskDescription());
        states.stream().forEach( tstates -> {
            System.out.println(tstates.getState().getData().getTaskDescription());
            taskDescription.add(tstates.getState().getData().getTaskDescription());
        });
        System.out.println("Task Description: "+taskDescription);
        return "Done";
    }
}