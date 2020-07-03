package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TemplateContract
import com.template.states.TemplateState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class Initiator(private val counterParty: Party, private val id: String, private val action: String, private val date: String, private val data: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // Create Output
        val output = TemplateState(issuer = ourIdentity, counterParty = counterParty, id = this.id, action = this.action, date = this.date, data = this.data)

        // Create command
        val cmd = Command(TemplateContract.Commands.Action(), listOf(ourIdentity.owningKey, counterParty.owningKey))

        // Create Transaction
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(output)
                .addCommand(cmd)

        txBuilder.verify(serviceHub)

        // Signing Transaction
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Gathering Signs
        val counterPartySession = initiateFlow(counterParty)
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, setOf(counterPartySession)))

        // Finalize Transaction
        return subFlow(FinalityFlow(fullySignedTx, setOf(counterPartySession)))
    }
}

@InitiatedBy(Initiator::class)
class Responder(val counterPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(counterPartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {}
        }
        val txId = subFlow(signTransactionFlow).id
        subFlow(ReceiveFinalityFlow(counterPartySession, expectedTxId = txId))
    }
}