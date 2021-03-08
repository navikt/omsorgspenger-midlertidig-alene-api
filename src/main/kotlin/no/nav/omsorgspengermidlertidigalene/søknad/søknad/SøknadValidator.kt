package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerMidlertidigAlene

internal fun Søknad.valider(k9Format: no.nav.k9.søknad.Søknad) {
    val mangler: MutableSet<Violation> = mutableSetOf()

    if (harBekreftetOpplysninger er false) {
        mangler.add(
            Violation(
                parameterName = "harBekreftetOpplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Opplysningene må bekreftes for å sende inn søknad.",
                invalidValue = harBekreftetOpplysninger
            )
        )
    }

    if (harForståttRettigheterOgPlikter er false) {
        mangler.add(
            Violation(
                parameterName = "harForståttRettigheterOgPlikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                invalidValue = harForståttRettigheterOgPlikter
            )
        )
    }

    if(barn.isEmpty()){
        mangler.add(
            Violation(
                parameterName = "barn",
                parameterType = ParameterType.ENTITY,
                reason = "Listen over barn kan ikke være tom",
                invalidValue = barn
            )
        )
    }

    barn.mapIndexed { index, barnSøknad -> mangler.addAll(barnSøknad.valider(index)) }

    mangler.addAll(annenForelder.valider())
    mangler.addAll(k9Format.valider())

    mangler.sortedBy { it.reason }

    if (mangler.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(mangler))
    }
}

fun no.nav.k9.søknad.Søknad.valider(): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf<Violation>()

    OmsorgspengerMidlertidigAlene().validator.valider(getYtelse<OmsorgspengerMidlertidigAlene>()).forEach {
        mangler.add(
            Violation(
                parameterName = it.felt,
                parameterType = ParameterType.ENTITY,
                reason = it.feilmelding,
                invalidValue = "k9-format feilkode: ${it.feilkode}"
            )
        )
    }

    return mangler
}

internal fun nullSjekk(verdi: Boolean?, navn: String): MutableSet<Violation>{
    val mangler: MutableSet<Violation> = mutableSetOf<Violation>()

    if(verdi er null){
        mangler.add(
            Violation(
                parameterName = navn,
                parameterType = ParameterType.ENTITY,
                reason = "$navn kan ikke være null",
                invalidValue = verdi
            )
        )
    }

    return mangler
}

internal infix fun Boolean?.er(forventetVerdi: Boolean?): Boolean = this == forventetVerdi