package no.nav.omsorgspengermidlertidigalene.søknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengermidlertidigalene.barn.BarnService
import no.nav.omsorgspengermidlertidigalene.felles.SØKNAD_URL
import no.nav.omsorgspengermidlertidigalene.felles.VALIDERING_URL
import no.nav.omsorgspengermidlertidigalene.felles.formaterStatuslogging
import no.nav.omsorgspengermidlertidigalene.general.CallId
import no.nav.omsorgspengermidlertidigalene.general.auth.IdToken
import no.nav.omsorgspengermidlertidigalene.general.auth.IdTokenProvider
import no.nav.omsorgspengermidlertidigalene.general.getCallId
import no.nav.omsorgspengermidlertidigalene.general.metadata
import no.nav.omsorgspengermidlertidigalene.k9format.tilK9Format
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import no.nav.omsorgspengermidlertidigalene.søker.SøkerService
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Søknad
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.valider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

fun Route.søknadApis(
    søknadService: SøknadService,
    barnService: BarnService,
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {
    post(SØKNAD_URL) {
        val søknad = call.receive<Søknad>()
        logger.info(formaterStatuslogging(søknad.søknadId, "mottatt"))

        val(idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)

        søknadService.registrer(
            søknad = søknad,
            metadata = call.metadata(),
            mottatt = mottatt,
            idToken = idToken,
            callId = callId
        )

        call.respond(HttpStatusCode.Accepted)
    }

    post(VALIDERING_URL) {
        val søknad = call.receive<Søknad>()
        val(idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)

        logger.trace("Henter søker")
        val søker: Søker = søkerService.getSøker(idToken = idToken, callId = callId)
        logger.trace("Søker hentet.")

        val listeOverBarnMedFnr = barnService.hentNåværendeBarn(idToken, callId)
        søknad.oppdaterBarnMedFnr(listeOverBarnMedFnr)

        val k9Format = søknad.tilK9Format(mottatt, søker)

        logger.trace("Validerer søknad...")
        søknad.valider(k9Format)
        logger.trace("Validering Ok.")
        call.respond(HttpStatusCode.Accepted)
    }
}

private fun ApplicationCall.hentIdTokenOgCallId(idTokenProvider: IdTokenProvider): Pair<IdToken, CallId> =
    Pair(idTokenProvider.getIdToken(this), getCallId())