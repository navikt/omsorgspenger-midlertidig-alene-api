package no.nav.omsorgspengermidlertidigalene.søknad

import no.nav.omsorgspengermidlertidigalene.felles.Metadata
import no.nav.omsorgspengermidlertidigalene.general.CallId
import no.nav.omsorgspengermidlertidigalene.general.auth.IdToken
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import no.nav.omsorgspengermidlertidigalene.søker.SøkerService
import no.nav.omsorgspengermidlertidigalene.søker.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class SøknadService(
    private val søkerService: SøkerService,
    private val kafkaProducer: SøknadKafkaProducer
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    suspend fun registrer(
        søknad: Søknad,
        metadata: Metadata,
        idToken: IdToken,
        callId: CallId
    ) {
        logger.info("Registrerer søknad med ID = {}", søknad.søknadId)

        logger.trace("Henter søker")
        val søker: Søker = søkerService.getSøker(idToken = idToken, callId = callId)
        logger.trace("Søker hentet.")

        logger.trace("Validerer søker.")
        søker.validate()
        logger.trace("Søker OK.")

        val komplettSøknad = søknad.tilKomplettSøknad(søker)

        kafkaProducer.produce(søknad = komplettSøknad, metadata = metadata)
        logger.info("Søknad: {}", komplettSøknad) //TODO Fjernes fra prod
    }
}