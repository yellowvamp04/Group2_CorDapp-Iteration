package com.template.states;

import com.template.contracts.ToDoContract;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(ToDoContract.class)
public class ToDoState implements LinearState {

    private final Party assignedBy;
    private final Party assignedTo;
    private final String taskDescription;
    private final UniqueIdentifier linearId;
    private final Date dateCreation;
    private final String deadline;

    public ToDoState(Party assignedBy, Party assignedTo, String taskDescription, Date dateCreation, String deadline) {
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
        this.linearId = new UniqueIdentifier();
        this.dateCreation = dateCreation;
        this.deadline = deadline;
    }

    public Party getAssignedBy() {
        return assignedBy;
    }

    public Party getAssignedTo() {
        return assignedTo;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public String getDeadline() { return deadline; }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(assignedBy, assignedTo);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

}