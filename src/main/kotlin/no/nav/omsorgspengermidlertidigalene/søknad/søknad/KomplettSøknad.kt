package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import no.nav.omsorgspengermidlertidigalene.søker.Søker
import java.time.ZonedDateTime

data class KomplettSøknad(
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val søknadId: String,
    val id: String,
    val språk: String,
    val arbeidssituasjon: List<Arbeidssituasjon>? = null, //TODO 26.02.2021 - Fjernes når frontend er prodsatt
    val annenForelder: AnnenForelder,
    val antallBarn: Int? = null, //TODO 26.02.2021 - Fjernes når frontend er prodsatt
    val fødselsårBarn: List<Int>? = null, //TODO 26.02.2021 - Fjernes når frontend er prodsatt
    val medlemskap: Medlemskap? = null, //TODO 26.02.2021 - Fjernes når frontend er prodsatt
    val barn: List<BarnSøknad>,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)