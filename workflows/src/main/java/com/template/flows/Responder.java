package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.sun.istack.NotNull;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(Initiator.class)
public class Responder extends FlowLogic<SignedTransaction> {
    private FlowSession counterPartySession;

    public Responder(FlowSession counterPartySession) {
        this.counterPartySession = counterPartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Responder flow logic goes here.
        final SignTransactionFlow signTransactionFlow = new SignTransactionFlow(counterPartySession) {
            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                System.out.println("check!!");
            }
        };

        SignedTransaction stx = subFlow(signTransactionFlow);

        return subFlow(new ReceiveFinalityFlow(counterPartySession, stx.getId()));
    }
}
