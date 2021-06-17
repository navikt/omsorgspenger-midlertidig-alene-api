package no.nav.omsorgspengermidlertidigalene.barn

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengermidlertidigalene.felles.BARN_URL
import no.nav.omsorgspengermidlertidigalene.general.auth.IdTokenProvider
import no.nav.omsorgspengermidlertidigalene.general.getCallId

fun Route.barnApis(
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {

    get(BARN_URL) {
        call.respond(
            BarnResponse(
                barnService.hentNåværendeBarn(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId()
                )
            )
        )
    }
}
