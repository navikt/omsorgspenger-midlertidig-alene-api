package no.nav.omsorgspengermidlertidigalene.barn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.omsorgspengermidlertidigalene.felles.k9SelvbetjeningOppslagKonfigurert
import no.nav.omsorgspengermidlertidigalene.general.CallId
import no.nav.omsorgspengermidlertidigalene.general.oppslag.K9OppslagGateway
import no.nav.omsorgspengermidlertidigalene.general.oppslag.throwable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate

class BarnGateway(
    baseUrl: URI,
    private val exchangeTokenClient: CachedAccessTokenClient,
    private val k9SelvbetjeningOppslagTokenxAudience: Set<String>
) : K9OppslagGateway(baseUrl) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger("nav.BarnGateway")
        private const val HENTE_BARN_OPERATION = "hente-barn"
        private val objectMapper = jacksonObjectMapper().k9SelvbetjeningOppslagKonfigurert()

        private val attributter = Pair(
            "a", listOf(
                "barn[].aktør_id",
                "barn[].fornavn",
                "barn[].mellomnavn",
                "barn[].etternavn",
                "barn[].fødselsdato",
                "barn[].identitetsnummer"
            )
        )

    }

    suspend fun hentBarn(
        idToken: IdToken,
        callId: CallId
    ): List<BarnOppslagDTO> {
        val exchangeToken = IdToken(exchangeTokenClient.getAccessToken(k9SelvbetjeningOppslagTokenxAudience, idToken.value).token)
        logger.info("Utvekslet token fra {} med token fra {}.", idToken.issuer(), exchangeToken.issuer())

        val barnUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("meg"),
            queryParameters = mapOf(
                attributter
            )
        ).toString()

        val httpRequest = generateHttpRequest(exchangeToken, barnUrl, callId)

        val oppslagRespons = Retry.retry(
            operation = HENTE_BARN_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "omsorgspenger-midlertidig-alene-api",
                operation = HENTE_BARN_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<BarnOppslagResponse>(success) },
                { error -> throw error.throwable(request, logger, "Feil ved henting av informasjon om søkers barn")}
            )
        }
        return oppslagRespons.barn
    }

    private data class BarnOppslagResponse(val barn: List<BarnOppslagDTO>)

    data class BarnOppslagDTO(
        val fødselsdato: LocalDate,
        val fornavn: String,
        val mellomnavn: String? = null,
        val etternavn: String,
        val aktør_id: String,
        val identitetsnummer: String? = null
    )

    override suspend fun check(): Result {
        val httpRequest = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("/isalive")
        ).toString()
            .httpGet()
            .header(
                HttpHeaders.Accept to "text/plain"
            )

        val (_, _, result) = Operation.monitored(
            app = "omsorgspenger-midlertidig-alene-api",
            operation = HENTE_BARN_OPERATION,
            resultResolver = { 200 == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        return result.fold(
            { _ -> Healthy("k9-selvbetjent-oppslag", "Helsesjekk mot k9-selvbetjent-oppslag OK.") },
            { error ->
                logger.error("Feil ved helsesjekk mot k9-selvbetjent-oppslag", error)
                UnHealthy("k9-selvbetjent-oppslag", "Helsesjekk mot k9-selvbetjent-oppslag feiler")
            }
        )
    }
}
