package com.template.states

import com.template.contracts.TemplateContract
import com.template.schemas.TemplateSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class TemplateState(
        val issuer: Party,
        val counterParty: Party,
        val id: String,
        val date: String,
        val action: String,
        val data: String,
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        override val participants: List<AbstractParty> = listOf(issuer, counterParty)
) : LinearState, QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is TemplateSchemaV1 -> TemplateSchemaV1.PersistentTemplate(this.issuer.name.toString(), this.counterParty.name.toString(), this.id, this.date, this.action, this.data)
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(TemplateSchemaV1)
}