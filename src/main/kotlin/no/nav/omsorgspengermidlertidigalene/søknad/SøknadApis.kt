package no.nav.omsorgspengermidlertidigalene.søknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengermidlertidigalene.barn.BarnService
import no.nav.omsorgspengermidlertidigalene.felles.SØKNAD_URL
import no.nav.omsorgspengermidlertidigalene.felles.VALIDERING_URL
import no.nav.omsorgspengermidlertidigalene.felles.formaterStatuslogging
import no.nav.omsorgspengermidlertidigalene.general.auth.IdTokenProvider
import no.nav.omsorgspengermidlertidigalene.general.getCallId
import no.nav.omsorgspengermidlertidigalene.general.metadata
import no.nav.omsorgspengermidlertidigalene.k9format.tilK9Format
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import no.nav.omsorgspengermidlertidigalene.søker.SøkerService
import no.nav.omsorgspengermidlertidigalene.søker.validate
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Søknad
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.valider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
fun Route.søknadApis(
    søknadService: SøknadService,
    barnService: BarnService,
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {

    @Location(SØKNAD_URL)
    class sendSøknad

    post { _ : sendSøknad ->
        logger.info("Mottatt ny søknad.")

        logger.trace("Mapper søknad")
        val søknad = call.receive<Søknad>()
        logger.trace("Søknad mappet.")

        logger.trace("Oppdaterer barn med identitetsnummer")
        val listeOverBarnMedFnr = barnService.hentNåværendeBarn(idTokenProvider.getIdToken(call), call.getCallId())
        søknad.oppdaterBarnMedFnr(listeOverBarnMedFnr)
        logger.info("Oppdatering av identitetsnummer på barn OK")

        val idToken = idTokenProvider.getIdToken(call)
        val callId = call.getCallId()
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)

        logger.trace("Henter søker")
        val søker: Søker = søkerService.getSøker(idToken = idToken, callId = callId)
        logger.trace("Søker hentet.")

        logger.trace("Validerer søker.")
        søker.validate()
        logger.trace("Søker OK.")

        logger.info("Mapper om til K9Format")
        val k9Format = søknad.tilK9Format(mottatt, søker)

        logger.trace("Validerer søknad")
        søknad.valider(k9Format)
        logger.trace("Validering OK.")

        logger.info(formaterStatuslogging(søknad.søknadId, "validert OK"))

        søknadService.registrer(
            søknad = søknad,
            metadata = call.metadata(),
            mottatt = mottatt,
            søker = søker,
            k9Format = k9Format
        )

        call.respond(HttpStatusCode.Accepted)
    }

    @Location(VALIDERING_URL)
    class validerSoknad

    post { _: validerSoknad ->
        val søknad = call.receive<Søknad>()
        val idToken = idTokenProvider.getIdToken(call)
        val callId = call.getCallId()
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)

        logger.trace("Henter søker")
        val søker: Søker = søkerService.getSøker(idToken = idToken, callId = callId)
        logger.trace("Søker hentet.")
        
        val k9Format = søknad.tilK9Format(mottatt, søker)

        logger.trace("Validerer søknad...")
        søknad.valider(k9Format)
        logger.trace("Validering Ok.")
        call.respond(HttpStatusCode.Accepted)
    }
}