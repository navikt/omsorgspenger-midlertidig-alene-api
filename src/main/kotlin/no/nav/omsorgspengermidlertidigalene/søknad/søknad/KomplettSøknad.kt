package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import no.nav.k9.søknad.Søknad
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import java.time.ZonedDateTime

data class KomplettSøknad(
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val søknadId: String,
    val id: String,
    val språk: String,
    val annenForelder: AnnenForelder,
    val barn: List<Barn>,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)