package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun Søknad.valider() {
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

    /* //TODO 26.02.2021 - Sett på når frontend er prodsatt
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
     */

    barn.mapIndexed { index, barnSøknad -> mangler.addAll(barnSøknad.valider(index)) }

    mangler.addAll(annenForelder.valider())

    if (mangler.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(mangler))
    }
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