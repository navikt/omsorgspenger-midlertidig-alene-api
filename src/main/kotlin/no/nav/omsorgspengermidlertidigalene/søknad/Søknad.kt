package no.nav.omsorgspengermidlertidigalene.søknad

import no.nav.omsorgspengermidlertidigalene.søker.Søker
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Søknad(
    val språk: String,
    val søknadId: String = UUID.randomUUID().toString(),
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
) {

    fun tilKomplettSøknad(søker: Søker): KomplettSøknad {
        return KomplettSøknad(
            språk = språk,
            søknadId = søknadId,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter
        )
    }
}