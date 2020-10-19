package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean,
    val utenlandsoppholdSiste12Mnd: List<Utenlandsopphold> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean,
    val utenlandsoppholdNeste12Mnd: List<Utenlandsopphold> = listOf()
)

internal fun Medlemskap.valider(): MutableSet<Violation> {
    val violations: MutableSet<Violation> = mutableSetOf()

    if(harBoddIUtlandetSiste12Mnd && utenlandsoppholdSiste12Mnd.isEmpty()){
        violations.add(
            Violation(
                parameterName = "utenlandsoppholdSiste12Mnd",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis harBoddIUtlandetSiste12Mnd er true så kan ikke utenlandsoppholdSiste12Mnd være en tom liste ",
                invalidValue = utenlandsoppholdSiste12Mnd
            )
        )
    }

    utenlandsoppholdSiste12Mnd.forEachIndexed{index, utenlandsopphold ->
        violations.addAll(utenlandsopphold.valider(relatertFelt = "medlemskap.utenlandsoppholdSiste12Mnd[$index]"))
    }

    if(skalBoIUtlandetNeste12Mnd && utenlandsoppholdNeste12Mnd.isEmpty()){
        violations.add(
            Violation(
                parameterName = "utenlandsoppholdNeste12Mnd",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis skalBoIUtlandetNeste12Mnd er true så kan ikke utenlandsoppholdNeste12Mnd være en tom liste ",
                invalidValue = utenlandsoppholdNeste12Mnd
            )
        )
    }

    utenlandsoppholdNeste12Mnd.forEachIndexed{index, utenlandsopphold ->
        violations.addAll(utenlandsopphold.valider(relatertFelt = "medlemskap.utenlandsoppholdNeste12Mnd[$index]"))
    }

    return violations
}