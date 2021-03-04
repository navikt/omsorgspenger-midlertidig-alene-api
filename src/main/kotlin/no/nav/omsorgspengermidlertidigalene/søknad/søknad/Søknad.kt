package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import no.nav.omsorgspengermidlertidigalene.barn.BarnOppslag
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Søknad(
    val søknadId: String = UUID.randomUUID().toString(),
    val id: String,
    val språk: String,
    val annenForelder: AnnenForelder,
    val barn: List<Barn> = listOf(),
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
) {
    fun tilKomplettSøknad(søker: Søker): KomplettSøknad = KomplettSøknad(
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            søknadId = søknadId,
            id = id,
            språk = språk,
            annenForelder = annenForelder,
            barn = barn,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter
        )

    fun oppdaterBarnMedFnr(listeOverBarnOppslag: List<BarnOppslag>) {
        barn.forEach { barn ->
            if (barn.manglerIdentitetsnummer()) {
                barn oppdaterIdentitetsnummerMed listeOverBarnOppslag.hentIdentitetsnummerForBarn(barn.aktørId)
            }
        }
    }
}

private fun List<BarnOppslag>.hentIdentitetsnummerForBarn(aktørId: String?): String? {
    return find {
        it.aktørId == aktørId
    }?.identitetsnummer
}