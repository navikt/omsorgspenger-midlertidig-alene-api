package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean? = null, //Settes til null for å unngå default false
    val utenlandsoppholdSiste12Mnd: List<Utenlandsopphold> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean? = null, //Settes til null for å unngå default false
    val utenlandsoppholdNeste12Mnd: List<Utenlandsopphold> = listOf()
)