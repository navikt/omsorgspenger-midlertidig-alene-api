package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import com.fasterxml.jackson.annotation.JsonAlias
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Søknad(
    val søknadId: String = UUID.randomUUID().toString(),
    val id: String,
    val språk: String,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val annenForelder: AnnenForelder,
    val antallBarn: Int,
    val fødselsårBarn: List<Int>,
    val medlemskap: Medlemskap,
    val harForståttRettigheterOgPlikter: Boolean? = null,
    val harBekreftetOpplysninger: Boolean? = null
) {
    fun tilKomplettSøknad(søker: Søker): KomplettSøknad {
        return KomplettSøknad(
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            søknadId = søknadId,
            id = id,
            språk = språk,
            arbeidssituasjon = arbeidssituasjon,
            annenForelder = annenForelder,
            antallBarn = antallBarn,
            fødselsårBarn = fødselsårBarn,
            medlemskap = medlemskap,
            harBekreftetOpplysninger = harBekreftetOpplysninger!!,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter!!
        )
    }
}

enum class Arbeidssituasjon(){
    @JsonAlias("selvstendigNæringsdrivende") SELVSTENDIG_NÆRINGSDRIVENDE,
    @JsonAlias("arbeidstaker") ARBEIDSTAKER,
    @JsonAlias("frilanser") FRILANSER,
    @JsonAlias("annen") ANNEN
}