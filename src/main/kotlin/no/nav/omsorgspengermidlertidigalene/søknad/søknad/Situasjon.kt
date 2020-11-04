package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import com.fasterxml.jackson.annotation.JsonAlias
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

enum class Situasjon {
    @JsonAlias("innlagtIHelseinstitusjon") INNLAGT_I_HELSEINSTITUSJON,
    @JsonAlias("utøverVerneplikt") UTØVER_VERNEPLIKT,
    @JsonAlias("fengsel") FENGSEL,
    @JsonAlias("sykdom") SYKDOM,
    @JsonAlias("annet") ANNET
}

internal fun AnnenForelder.validerSituasjon(): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf()

    when(situasjon){
        Situasjon.INNLAGT_I_HELSEINSTITUSJON -> mangler.addAll(validerInnlagtIHelseinstitusjon())
        Situasjon.UTØVER_VERNEPLIKT, Situasjon.FENGSEL -> mangler.addAll(validerDato())
        Situasjon.SYKDOM, Situasjon.ANNET -> {
            mangler.addAll(validerSituasjonBeskrivelse())
            mangler.addAll(validerPeriodeOver6MånederSatt())
        }
    }

    return mangler
}

private fun AnnenForelder.validerPeriodeOver6MånederSatt(): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf()

    if(periodeOver6Måneder er null){
        mangler.add(
            Violation(
                parameterName = "AnnenForelder.periodeOver6Måneder",
                parameterType = ParameterType.ENTITY,
                reason = "periodeOver6Måneder kan ikke være null",
                invalidValue = periodeOver6Måneder
            )
        )
    }

    return mangler
}

private fun AnnenForelder.validerDato(): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf()

    if(periodeFraOgMed == null){
        mangler.add(
            Violation(
                parameterName = "AnnenForelder.periodeFraOgMed",
                parameterType = ParameterType.ENTITY,
                reason = "periodeFraOgMed kan ikke være null dersom situasjonen er fengsel eller verneplikt",
                invalidValue = periodeFraOgMed
            )
        )
    }

    if(periodeTilOgMed == null){
        mangler.add(
            Violation(
                parameterName = "AnnenForelder.periodeTilOgMed",
                parameterType = ParameterType.ENTITY,
                reason = "periodeTilOgMed kan ikke være null dersom situasjonen er fengsel eller verneplikt",
                invalidValue = periodeTilOgMed
            )
        )
    }

    if(periodeFraOgMed != null && periodeTilOgMed != null){
        if(periodeFraOgMed.isAfter(periodeTilOgMed)){
            mangler.add(
                Violation(
                    parameterName = "AnnenForelder.periodeFraOgMed",
                    parameterType = ParameterType.ENTITY,
                    reason = "periodeFraOgMed kan ikke være etter periodeTilOgMed",
                    invalidValue = periodeFraOgMed
                )
            )
        }
    }

    return mangler
}

private fun AnnenForelder.validerSituasjonBeskrivelse(): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf()

    if(situasjonBeskrivelse.isNullOrBlank()){
        mangler.add(
            Violation(
                parameterName = "AnnenForelder.situasjonBeskrivelse",
                parameterType = ParameterType.ENTITY,
                reason = "Situasjonsbeskrivelse på annenForelder kan ikke være null, tom eller kun white spaces ved SYKDOM eller ANNET",
                invalidValue = situasjonBeskrivelse
            )
        )
    }

    return mangler
}

private fun AnnenForelder.validerInnlagtIHelseinstitusjon(): MutableSet<Violation>{
    val mangler: MutableSet<Violation> = mutableSetOf()

    if(vetLengdePåInnleggelseperioden == null){
        mangler.add(
            Violation(
                parameterName = "AnnenForelder.vetLengdePåInnleggelseperioden",
                parameterType = ParameterType.ENTITY,
                reason = "vetLengdePåInnleggelseperioden kan ikke være null",
                invalidValue = vetLengdePåInnleggelseperioden
            )
        )
    } else {
        if(vetLengdePåInnleggelseperioden) mangler.addAll(validerDato())
        else mangler.addAll(validerPeriodeOver6MånederSatt())
    }

    return mangler
}