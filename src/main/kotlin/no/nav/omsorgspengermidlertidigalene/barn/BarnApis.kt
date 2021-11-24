package no.nav.omsorgspengermidlertidigalene.barn

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengermidlertidigalene.felles.BARN_URL
import no.nav.omsorgspengermidlertidigalene.general.auth.IdTokenProvider
import no.nav.omsorgspengermidlertidigalene.general.getCallId
import no.nav.omsorgspengermidlertidigalene.general.oppslag.TilgangNektetException
import no.nav.omsorgspengermidlertidigalene.general.oppslag.respondTilgangNektetProblemDetail
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.omsorgspengermidlertidigalene.barn.barnApis")

fun Route.barnApis(
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {

    get(BARN_URL) {
        try {
            call.respond(
                BarnResponse(
                    barnService.hentNåværendeBarn(
                        idToken = idTokenProvider.getIdToken(call),
                        callId = call.getCallId()
                    )
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
