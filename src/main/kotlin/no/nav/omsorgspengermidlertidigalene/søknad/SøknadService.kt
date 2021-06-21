package no.nav.omsorgspengermidlertidigalene.søknad

import no.nav.omsorgspengermidlertidigalene.barn.BarnService
import no.nav.omsorgspengermidlertidigalene.felles.Metadata
import no.nav.omsorgspengermidlertidigalene.felles.formaterStatuslogging
import no.nav.omsorgspengermidlertidigalene.general.CallId
import no.nav.omsorgspengermidlertidigalene.general.auth.IdToken
import no.nav.omsorgspengermidlertidigalene.k9format.tilK9Format
import no.nav.omsorgspengermidlertidigalene.kafka.SøknadKafkaProducer
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import no.nav.omsorgspengermidlertidigalene.søker.SøkerService
import no.nav.omsorgspengermidlertidigalene.søker.validate
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Søknad
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.valider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime


class SøknadService(
    private val søkerService: SøkerService,
    val barnService: BarnService,
    private val kafkaProducer: SøknadKafkaProducer
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    suspend fun registrer(
        søknad: Søknad,
        metadata: Metadata,
        mottatt: ZonedDateTime,
        idToken: IdToken,
        callId: CallId,
    ) {
        logger.info(formaterStatuslogging(søknad.søknadId, "registreres"))

        val søker: Søker = søkerService.getSøker(idToken = idToken, callId = callId)
        søker.validate()

        val listeOverBarnMedFnr = barnService.hentNåværendeBarn(idToken, callId)
        søknad.oppdaterBarnMedFnr(listeOverBarnMedFnr)

        logger.info("Mapper om til K9Format")
        val k9Format = søknad.tilK9Format(mottatt, søker)
        søknad.valider(k9Format)

        val komplettSøknad = søknad.tilKomplettSøknad(søker, mottatt, k9Format)
        kafkaProducer.produserKafkaMelding(søknad = komplettSøknad, metadata = metadata)
    }
}