package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
)

internal fun Utenlandsopphold.valider(relatertFelt: String): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf()

    if(tilOgMed.isBefore(fraOgMed)){
        mangler.add(
            Violation(
                parameterName = "$relatertFelt.tilOgMed",
                parameterType = ParameterType.ENTITY,
                reason = "tilOgMed kan ikke være før fraOgMed",
                invalidValue = tilOgMed
            )
        )
    }

    if(landkode.isNullOrBlank()){
        mangler.add(
            Violation(
                parameterName = "$relatertFelt.landkode",
                parameterType = ParameterType.ENTITY,
                reason = "Landkode er ikke gyldig",
                invalidValue = landkode
            )
        )
    }

    if(landnavn.isNullOrBlank()){
        mangler.add(
            Violation(
                parameterName = "$relatertFelt.landnavn",
                parameterType = ParameterType.ENTITY,
                reason = "Landnavn er ikke gyldig",
                invalidValue = landnavn
            )
        )
    }

    return mangler
}

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

