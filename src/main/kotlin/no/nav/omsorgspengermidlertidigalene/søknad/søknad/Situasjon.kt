package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import com.fasterxml.jackson.annotation.JsonAlias
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

enum class Situasjon {
    @JsonAlias("innlagtIHelseinstitusjon") INNLAGT_I_HELSEINSTITUSJON,
    @JsonAlias("utøverVerneplikt") UTØVER_VERNEPLIKT,
    @JsonAlias("fengsel") FENGSEL,
    @JsonAlias("sykdom") SYKDOM,
    @JsonAlias("annet") ANNET
}

internal fun AnnenForelder.validerSituasjon(): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf()

    var validerBeskrivelse = false
    var validerPeriodeOver6Måneder = false
    var validerDato = false
    var validerDatoEllerOver6Måneder = false

    when (situasjon) {
        Situasjon.INNLAGT_I_HELSEINSTITUSJON -> validerDatoEllerOver6Måneder = true
        Situasjon.UTØVER_VERNEPLIKT, Situasjon.FENGSEL -> validerDato = true
        Situasjon.SYKDOM, Situasjon.ANNET -> {
            validerBeskrivelse = true
            validerPeriodeOver6Måneder = true
        }
    }

    if(validerBeskrivelse) mangler.addAll(validerSituasjonBeskrivelse(situasjonBeskrivelse))

    if(validerPeriodeOver6Måneder) mangler.addAll(validerPeriodeOver6Måneder(periodeOver6Måneder))

    if(validerDato) mangler.addAll(validerDato(periodeFraOgMed, periodeTilOgMed))

    if(validerDatoEllerOver6Måneder){
        if(periodeOver6Måneder er null && periodeFraOgMed == null && periodeTilOgMed == null){
            mangler.add(
                Violation(
                    parameterName = "annenForelder.periodeOver6Måneder",
                    parameterType = ParameterType.ENTITY,
                    reason = "periodeOver6Måneder ELLER periodeFraOgMed og periodeTilOgMed må settes",
                    invalidValue = periodeOver6Måneder
                )
            )
        } else if(periodeOver6Måneder er null) {
            mangler.addAll(validerDato(periodeFraOgMed = periodeFraOgMed, periodeTilOgMed = periodeTilOgMed))
        } else mangler.addAll(validerPeriodeOver6Måneder(periodeOver6Måneder))
    }

    return mangler
}

private fun validerPeriodeOver6Måneder(periodeOver6Måneder: Boolean?): MutableSet<Violation> {
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

    if(periodeOver6Måneder er false){
        mangler.add(
            Violation(
                parameterName = "AnnenForelder.periodeOver6Måneder",
                parameterType = ParameterType.ENTITY,
                reason = "periodeOver6Måneder kan ikke være false",
                invalidValue = periodeOver6Måneder
            )
        )
    }

    return mangler
}

private fun validerDato(periodeFraOgMed: LocalDate?, periodeTilOgMed: LocalDate?): MutableSet<Violation> {
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

private fun validerSituasjonBeskrivelse(situasjonBeskrivelse: String?): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf()

    if(situasjonBeskrivelse.isNullOrBlank()){
        mangler.add(
            Violation(
                parameterName = "AnnenForelder.situasjonBeskrivelse",
                parameterType = ParameterType.ENTITY,
                reason = "Situasjonsbeskrivelse på annenForelder kan ikke være null eller tom eller kun white spaces ved SYKDOM eller ANNET",
                invalidValue = situasjonBeskrivelse
            )
        )
    }

    return mangler
}