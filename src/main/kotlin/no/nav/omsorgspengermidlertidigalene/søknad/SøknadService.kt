package no.nav.omsorgspengermidlertidigalene.søknad

import no.nav.omsorgspengermidlertidigalene.felles.Metadata
import no.nav.omsorgspengermidlertidigalene.general.CallId
import no.nav.omsorgspengermidlertidigalene.general.auth.IdToken
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import no.nav.omsorgspengermidlertidigalene.søker.SøkerService
import no.nav.omsorgspengermidlertidigalene.søker.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime


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
        logger.info("Registrerer søknad.")

        logger.trace("Henter søker")
        val søker: Søker = søkerService.getSøker(idToken = idToken, callId = callId)
        logger.trace("Søker hentet.")

        logger.trace("Validerer søker.")
        søker.validate()
        logger.trace("Søker OK.")

        val komplettSoknad = KomplettSøknad(
            språk = søknad.språk,
            søknadId = søknad.søknadId,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            harBekreftetOpplysninger = søknad.harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter
        )

        logger.info("Legger søknad med ID = {} til prosessering", søknad.søknadId)
        kafkaProducer.produce(søknad = komplettSoknad, metadata = metadata)
        logger.info("Søknad: {}", komplettSoknad) //TODO Fjernes fra prod
    }
}