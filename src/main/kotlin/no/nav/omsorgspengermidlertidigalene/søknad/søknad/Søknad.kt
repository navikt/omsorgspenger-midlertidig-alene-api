package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import com.fasterxml.jackson.annotation.JsonAlias
import no.nav.omsorgspengermidlertidigalene.barn.Barn
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Søknad(
    val søknadId: String = UUID.randomUUID().toString(),
    val id: String,
    val språk: String,
    val arbeidssituasjon: List<Arbeidssituasjon>? = null, //TODO 26.02.2021 - Fjernes når frontend er prodsatt
    val annenForelder: AnnenForelder,
    val antallBarn: Int? = null, //TODO 26.02.2021 - Fjernes når frontend er prodsatt
    val fødselsårBarn: List<Int>? = null, //TODO 26.02.2021 - Fjernes når frontend er prodsatt
    val medlemskap: Medlemskap? = null, //TODO 26.02.2021 - Fjernes når frontend er prodsatt
    val barn: List<BarnSøknad> = listOf(),
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
) {
    fun tilKomplettSøknad(søker: Søker): KomplettSøknad = KomplettSøknad(
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
            barn = barn,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter
        )

    fun oppdaterBarnMedFnr(listeOverBarn: List<Barn>) {
        barn.forEach { barn ->
            if (barn.manglerIdentitetsnummer()) {
                barn oppdaterIdentitetsnummerMed listeOverBarn.hentIdentitetsnummerForBarn(barn.aktørId)
            }
        }
    }
}

enum class Arbeidssituasjon(){
    @JsonAlias("selvstendigNæringsdrivende") SELVSTENDIG_NÆRINGSDRIVENDE,
    @JsonAlias("arbeidstaker") ARBEIDSTAKER,
    @JsonAlias("frilanser") FRILANSER,
    @JsonAlias("annen") ANNEN
}

private fun List<Barn>.hentIdentitetsnummerForBarn(aktørId: String?): String? {
    return find {
        it.aktørId == aktørId
    }?.identitetsnummer
}