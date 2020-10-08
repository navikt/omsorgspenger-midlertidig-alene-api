package no.nav.omsorgspengermidlertidigalene.general

import io.ktor.application.*
import io.ktor.features.*

data class CallId(val value : String)

fun ApplicationCall.getCallId() : CallId {
    return CallId(callId!!)
}
