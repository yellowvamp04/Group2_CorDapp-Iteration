package main.java.com.template.contracts;

import main.java.com.template.states.ToDoState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class ToDoContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.TemplateContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands.Create> command = requireSingleCommand(tx.getCommands(), Commands.Create.class);

        requireThat(require -> {
            final ToDoState out = tx.outputsOfType(ToDoState.class).get(0);
            require.using("Iteration1: Task Description should not be empty.",
                    !out.getTaskDescription().trim().isEmpty());

            require.using("Iteration1: Task Description can't exceed 40 characters!",
                    out.getTaskDescription().length() <= 40);

            require.using("Iteration1: You can't be the assignedTo!",
                    !out.getAssignedBy().equals(out.getAssignedTo()));

            return null;
        });
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Create implements Commands {}
    }
}