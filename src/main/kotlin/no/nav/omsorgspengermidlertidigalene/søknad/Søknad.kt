package no.nav.omsorgspengermidlertidigalene.søknad

import java.util.*

data class Søknad(
    val språk: String,
    val søknadId: String = UUID.randomUUID().toString(),
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)