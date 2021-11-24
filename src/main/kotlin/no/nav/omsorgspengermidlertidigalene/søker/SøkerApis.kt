package no.nav.omsorgspengermidlertidigalene.søker

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengermidlertidigalene.felles.SØKER_URL
import no.nav.omsorgspengermidlertidigalene.general.auth.IdTokenProvider
import no.nav.omsorgspengermidlertidigalene.general.getCallId
import no.nav.omsorgspengermidlertidigalene.general.oppslag.TilgangNektetException
import no.nav.omsorgspengermidlertidigalene.general.oppslag.respondTilgangNektetProblemDetail
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("no.nav.omsorgspengermidlertidigalene.søker.søkerApis")
fun Route.søkerApis(
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {

    get(SØKER_URL) {
        try {
            call.respond(
                søkerService.getSøker(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId()
                )
            )
        } catch (e: Exception) {
            when(e){
                is TilgangNektetException -> call.respondTilgangNektetProblemDetail(logger, e)
                else -> throw e
            }
        }
    }
}

