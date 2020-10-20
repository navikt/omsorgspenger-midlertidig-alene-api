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
    val alderAvAlleBarn: List<Int>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden, //TODO Sender frontend null, eller sender de med objektet også er heller felter der false/tom?
    val harForståttRettigheterOgPlikter: Boolean? = null, //Settes til null for å unngå default false
    val harBekreftetOpplysninger: Boolean? = null //Settes til null for å unngå default false
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
            alderAvAlleBarn = alderAvAlleBarn,
            medlemskap = medlemskap,
            utenlandsoppholdIPerioden = utenlandsoppholdIPerioden,
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