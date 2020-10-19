package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

data class UtenlandsoppholdIPerioden(
    val skalOppholdeSegIUtlandetIPerioden: Boolean,
    val opphold: List<Utenlandsopphold> = listOf() //TODO Sender frontend null eller tom liste?
)

internal fun UtenlandsoppholdIPerioden.valider(): MutableSet<Violation>{
    val mangler: MutableSet<Violation> = mutableSetOf()

    if(skalOppholdeSegIUtlandetIPerioden && opphold.isEmpty()){
        mangler.add(
            Violation(
                parameterName = "UtenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis skalOppholdeSegIUtlandetIPerioden er true så kan ikke opphold være en tom liste",
                invalidValue = skalOppholdeSegIUtlandetIPerioden
            )
        )
    }

    if(skalOppholdeSegIUtlandetIPerioden er false && opphold.isNotEmpty()){
        mangler.add(
            Violation(
                parameterName = "UtenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis skalOppholdeSegIUtlandetIPerioden er false så kan ikke opphold ha elementer i listen",
                invalidValue = skalOppholdeSegIUtlandetIPerioden
            )
        )
    }

    opphold.forEachIndexed{index, utenlandsopphold ->
        mangler.addAll(utenlandsopphold.valider(relatertFelt = "utenlandsoppholdIPerioden.opphold[$index]"))
    }

    return mangler
}