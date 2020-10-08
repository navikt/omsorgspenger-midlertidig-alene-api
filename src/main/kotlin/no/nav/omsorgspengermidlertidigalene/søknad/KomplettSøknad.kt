package no.nav.omsorgspengermidlertidigalene.søknad

import no.nav.omsorgspengermidlertidigalene.søker.Søker
import java.time.ZonedDateTime

data class KomplettSøknad(
    val språk: String,
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)