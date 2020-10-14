package no.nav.omsorgspengermidlertidigalene.søker

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengermidlertidigalene.felles.SØKER_URL
import no.nav.omsorgspengermidlertidigalene.general.auth.IdTokenProvider
import no.nav.omsorgspengermidlertidigalene.general.getCallId

@KtorExperimentalLocationsAPI
fun Route.søkerApis(
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {

    @Location(SØKER_URL)
    class getSoker

    get { _: getSoker ->
        call.respond(
            søkerService.getSøker(
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId()
            )
        )
    }
}

