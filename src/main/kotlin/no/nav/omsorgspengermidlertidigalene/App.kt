package no.nav.omsorgspengermidlertidigalene

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.metrics.micrometer.*
import io.ktor.routing.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.dusseldorf.ktor.auth.*
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.dusseldorf.ktor.metrics.init
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.omsorgspengermidlertidigalene.barn.BarnGateway
import no.nav.omsorgspengermidlertidigalene.barn.BarnService
import no.nav.omsorgspengermidlertidigalene.barn.barnApis
import no.nav.omsorgspengermidlertidigalene.general.systemauth.AccessTokenClientResolver
import no.nav.omsorgspengermidlertidigalene.kafka.SøknadKafkaProducer
import no.nav.omsorgspengermidlertidigalene.mellomlagring.MellomlagringService
import no.nav.omsorgspengermidlertidigalene.mellomlagring.mellomlagringApis
import no.nav.omsorgspengermidlertidigalene.redis.RedisConfig
import no.nav.omsorgspengermidlertidigalene.redis.RedisStore
import no.nav.omsorgspengermidlertidigalene.søker.SøkerGateway
import no.nav.omsorgspengermidlertidigalene.søker.SøkerService
import no.nav.omsorgspengermidlertidigalene.søker.søkerApis
import no.nav.omsorgspengermidlertidigalene.søknad.SøknadService
import no.nav.omsorgspengermidlertidigalene.søknad.søknadApis
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val logger: Logger = LoggerFactory.getLogger("nav.omsorgpengermidlertidigaleneapi")

fun Application.omsorgpengermidlertidigaleneapi() {
    
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    System.setProperty("dusseldorf.ktor.serializeProblemDetailsWithContentNegotiation", "true")

    val configuration = Configuration(environment.config)
    val accessTokenClientResolver = AccessTokenClientResolver(environment.config.clients())
    val tokenxClient = CachedAccessTokenClient(accessTokenClientResolver.tokenxClient)

    install(ContentNegotiation) {
        jackson {
            dusseldorfConfigured()
                .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
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
    val issuers = configuration.issuers()

    install(Authentication) {
        multipleJwtIssuers(
            issuers = issuers,
            extractHttpAuthHeader = { call -> idTokenProvider.getIdToken(call).somHttpAuthHeader() }
        )
    }

    install(StatusPages) {
        DefaultStatusPages()
        JacksonStatusPages()
        IdTokenStatusPages()
    }

    install(Routing) {

        val søkerGateway = SøkerGateway(
            baseUrl = configuration.getK9OppslagUrl(),
            exchangeTokenClient = tokenxClient,
            k9SelvbetjeningOppslagTokenxAudience = configuration.getK9SelvbetjeningOppslagTokenxAudience()
        )

        val søkerService = SøkerService(
            søkerGateway = søkerGateway
        )

        val søknadKafkaProducer = SøknadKafkaProducer(
            kafkaConfig = configuration.getKafkaConfig()
        )

        val barnGateway = BarnGateway(
            baseUrl = configuration.getK9OppslagUrl(),
            exchangeTokenClient = tokenxClient,
            k9SelvbetjeningOppslagTokenxAudience = configuration.getK9SelvbetjeningOppslagTokenxAudience()
        )

        val barnService = BarnService(
            barnGateway = barnGateway,
            cache = configuration.cache()
        )

        environment.monitor.subscribe(ApplicationStopping) {
            logger.info("Stopper Kafka Producer.")
            søknadKafkaProducer.stop()
            logger.info("Kafka Producer Stoppet.")
        }

        authenticate(*issuers.allIssuers())  {

            søkerApis(
                søkerService = søkerService,
                idTokenProvider = idTokenProvider
            )

            barnApis(
                barnService = barnService,
                idTokenProvider = idTokenProvider
            )

            mellomlagringApis(
                mellomlagringService = MellomlagringService(
                    RedisStore(
                        redisClient = RedisConfig.redisClient(
                            redisHost = configuration.getRedisHost(),
                            redisPort = configuration.getRedisPort()
                        )
                    ),
                    passphrase = configuration.getStoragePassphrase(),
                ),
                idTokenProvider = idTokenProvider
            )

            søknadApis(
                idTokenProvider = idTokenProvider,
                barnService = barnService,
                søkerService = søkerService,
                søknadService = SøknadService(
                    søkerService = søkerService,
                    barnService = barnService,
                    kafkaProducer = søknadKafkaProducer
                )
            )
        }

        val healthService = HealthService(
            healthChecks = setOf(
                søknadKafkaProducer,
                søkerGateway,
                barnGateway
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
