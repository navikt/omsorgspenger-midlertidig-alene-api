package no.nav.omsorgspengermidlertidigalene.barn

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengermidlertidigalene.felles.BARN_URL
import no.nav.omsorgspengermidlertidigalene.general.auth.IdTokenProvider
import no.nav.omsorgspengermidlertidigalene.general.getCallId

@KtorExperimentalLocationsAPI
fun Route.barnApis(
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {

    @Location(BARN_URL)
    class getBarn

    get { _: getBarn ->
        call.respond(
            BarnResponse(
                barnService.hentNaaverendeBarn(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId()
                )
            )
        )
    }
}
