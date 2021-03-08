package no.nav.omsorgspengermidlertidigalene.søknad

import no.nav.omsorgspengermidlertidigalene.felles.Metadata
import no.nav.omsorgspengermidlertidigalene.felles.formaterStatuslogging
import no.nav.omsorgspengermidlertidigalene.kafka.SøknadKafkaProducer
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import no.nav.omsorgspengermidlertidigalene.søker.SøkerService
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Søknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        mottatt: ZonedDateTime,
        søker: Søker,
        k9Format: no.nav.k9.søknad.Søknad
    ) {
        logger.info(formaterStatuslogging(søknad.søknadId, "registreres"))

        val komplettSøknad = søknad.tilKomplettSøknad(søker, mottatt, k9Format)

        kafkaProducer.produce(søknad = komplettSøknad, metadata = metadata)
    }
}