package no.nav.omsorgspengermidlertidigalene

import com.github.fppt.jedismock.RedisServer
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.asArguments
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.omsorgspengermidlertidigalene.mellomlagring.started
import no.nav.omsorgspengermidlertidigalene.wiremock.omsorgspengerMidlertidigAleneApiConfig
import no.nav.omsorgspengermidlertidigalene.wiremock.stubK9OppslagSoker
import no.nav.omsorgspengermidlertidigalene.wiremock.stubOppslagHealth
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ApplicationWithMocks {
    companion object {

        private val logger: Logger = LoggerFactory.getLogger(ApplicationWithMocks::class.java)

        @JvmStatic
        fun main(args: Array<String>) {

            val wireMockServer = WireMockBuilder()
                .withPort(8081)
                .withAzureSupport()
                .withNaisStsSupport()
                .withLoginServiceSupport()
                .omsorgspengerMidlertidigAleneApiConfig()
                .build()
                .stubOppslagHealth()
                .stubK9OppslagSoker()

            val redisServer: RedisServer = RedisServer
                .newRedisServer(6379)
                .started()

            val testArgs = TestConfiguration.asMap(
                port = 8082,
                wireMockServer = wireMockServer,
                redisServer = redisServer
            ).asArguments()

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    logger.info("Tearing down")
                    wireMockServer.stop()
                    logger.info("Tear down complete")
                }
            })

            withApplication { no.nav.omsorgspengermidlertidigalene.main(testArgs) }
        }
    }
}