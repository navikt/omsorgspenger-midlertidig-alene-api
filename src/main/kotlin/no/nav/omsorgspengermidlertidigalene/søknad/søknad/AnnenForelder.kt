package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class AnnenForelder(
    val navn: String,
    val fnr: String,
    val situasjon: Situasjon,
    val situasjonBeskrivelse: String,
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeFraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeTilOgMed: LocalDate
)

enum class Situasjon(){
    @JsonAlias("innlagtIHelseinstitusjon") INNLAGT_I_HELSEINSTITUSJON,
    @JsonAlias("utøverVerneplikt") UTØVER_VERNEPLIKT,
    @JsonAlias("fengsel") FENGSEL,
    @JsonAlias("sykdom") SYKDOM,
    @JsonAlias("annet") ANNET
}

internal fun AnnenForelder.valider(): MutableSet<Violation> {
    val violations: MutableSet<Violation> = mutableSetOf()

    if(navn.isNullOrBlank()){
        violations.add(
            Violation(
                parameterName = "AnnenForelder.navn",
                parameterType = ParameterType.ENTITY,
                reason = "Navn på annen forelder kan ikke være null eller tom eller kun white spaces",
                invalidValue = navn
            )
        )
    }

    if(fnr.erGyldigNorskIdentifikator() er false){
        violations.add(
            Violation(
                parameterName = "AnnenForelder.fnr",
                parameterType = ParameterType.ENTITY,
                reason = "Fødselsnummer på annen forelder må være gyldig norsk identifikator",
                invalidValue = fnr
            )
        )
    }

    return violations
}