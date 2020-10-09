package no.nav.omsorgspengermidlertidigalene

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.metrics.micrometer.*
import io.ktor.routing.*
import io.ktor.util.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthConfig
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.dusseldorf.ktor.metrics.init
import no.nav.omsorgspengermidlertidigalene.barn.BarnGateway
import no.nav.omsorgspengermidlertidigalene.barn.BarnService
import no.nav.omsorgspengermidlertidigalene.barn.barnApis
import no.nav.omsorgspengermidlertidigalene.general.auth.IdTokenProvider
import no.nav.omsorgspengermidlertidigalene.general.auth.authorizationStatusPages
import no.nav.omsorgspengermidlertidigalene.søker.SøkerGateway
import no.nav.omsorgspengermidlertidigalene.søker.SøkerService
import no.nav.omsorgspengermidlertidigalene.søker.søkerApis
import no.nav.omsorgspengermidlertidigalene.søknad.SøknadKafkaProducer
import no.nav.omsorgspengermidlertidigalene.søknad.SøknadService
import no.nav.omsorgspengermidlertidigalene.søknad.søknadApis
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val logger: Logger = LoggerFactory.getLogger("nav.omsorgpengermidlertidigaleneapi")

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.omsorgpengermidlertidigaleneapi() {
    
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    val configuration = Configuration(environment.config)
    val apiGatewayApiKey = configuration.getApiGatewayApiKey()

    install(ContentNegotiation) {
        jackson {
            dusseldorfConfigured()
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
                .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        allowNonSimpleContentTypes = true
        allowCredentials = true
        log.info("Configuring CORS")
        configuration.getWhitelistedCorsAddreses().forEach {
            log.info("Adding host {} with scheme {}", it.host, it.scheme)
            host(host = it.authority, schemes = listOf(it.scheme))
        }
    }

    val idTokenProvider = IdTokenProvider(cookieName = configuration.getCookieName())
    val jwkProvider = JwkProviderBuilder(configuration.getJwksUrl().toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt {
            realm = appId
            verifier(jwkProvider, configuration.getIssuer()) {
                acceptNotBefore(10)
                acceptIssuedAt(10)
            }
            authHeader { call ->
                idTokenProvider
                    .getIdToken(call)
                    .medValidertLevel("Level4")
                    .somHttpAuthHeader()
            }
            validate { credentials ->
                return@validate JWTPrincipal(credentials.payload)
            }
        }
    }

    install(StatusPages) {
        DefaultStatusPages()
        JacksonStatusPages()
        authorizationStatusPages()
    }

    install(Locations)

    install(Routing) {

        val søkerGateway = SøkerGateway(
            baseUrl = configuration.getK9OppslagUrl(),
            apiGatewayApiKey = apiGatewayApiKey
        )

        val barnGateway = BarnGateway(
            baseUrl = configuration.getK9OppslagUrl(),
            apiGatewayApiKey = apiGatewayApiKey
        )

        val søkerService = SøkerService(
            søkerGateway = søkerGateway
        )

        val søknadKafkaProducer = SøknadKafkaProducer(
            kafkaConfig = configuration.getKafkaConfig()
        )

        environment.monitor.subscribe(ApplicationStopping) {
            logger.info("Stopper Kafka Producer.")
            søknadKafkaProducer.stop()
            logger.info("Kafka Producer Stoppet.")
        }

        authenticate {

            søkerApis(
                søkerService = søkerService,
                idTokenProvider = idTokenProvider
            )

            barnApis(
                barnService = BarnService(
                    barnGateway = barnGateway
                ),
                idTokenProvider = idTokenProvider
            )

            //TODO: Fix redis når dette er tilgjengelig i GCP
/*            mellomlagringApis(
                mellomlagringService = MellomlagringService(
                    RedisStore(
                        RedisConfig(
                            RedisConfigurationProperties(
                                configuration.getRedisHost().equals("localhost")
                            )
                        ).redisClient(configuration)
                    ), configuration.getStoragePassphrase()),
                idTokenProvider = idTokenProvider
            )*/

            søknadApis(
                idTokenProvider = idTokenProvider,
                søknadService = SøknadService(
                    søkerService = søkerService,
                    kafkaProducer = søknadKafkaProducer
                )
            )
        }

        val healthService = HealthService(
            healthChecks = setOf(
                søknadKafkaProducer,
                HttpRequestHealthCheck(mapOf(
                    configuration.getJwksUrl() to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK, includeExpectedStatusEntity = false)
                ))
            )
        )

        HealthReporter(
            app = appId,
            healthService = healthService,
            frequency = Duration.ofMinutes(1)
        )

        DefaultProbeRoutes()
        MetricsRoute()
        HealthRoute(
            healthService = healthService
        )
    }

    install(MicrometerMetrics) {
        init(appId)
    }

    intercept(ApplicationCallPipeline.Monitoring) {
        call.request.log()
    }

    install(CallId) {
        generated()
    }

    install(CallLogging) {
        correlationIdAndRequestIdInMdc()
        logRequests()
        mdc("id_token_jti") { call ->
            try { idTokenProvider.getIdToken(call).getId() }
            catch (cause: Throwable) { null }
        }
    }
}

fun ObjectMapper.k9SelvbetjeningOppslagKonfigurert(): ObjectMapper {
    return jacksonObjectMapper().dusseldorfConfigured().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        registerModule(JavaTimeModule())
        propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
    }
}