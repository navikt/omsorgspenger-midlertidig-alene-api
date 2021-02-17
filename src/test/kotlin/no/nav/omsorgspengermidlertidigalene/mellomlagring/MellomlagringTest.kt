package no.nav.omsorgspengermidlertidigalene.mellomlagring

import com.github.fppt.jedismock.RedisServer
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.util.*
import no.nav.omsorgspengermidlertidigalene.Configuration
import no.nav.omsorgspengermidlertidigalene.TestConfiguration
import no.nav.omsorgspengermidlertidigalene.redis.RedisConfig
import no.nav.omsorgspengermidlertidigalene.redis.RedisMockUtil
import no.nav.omsorgspengermidlertidigalene.redis.RedisStore
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.AfterClass
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.test.*

@KtorExperimentalAPI
class MellomlagringTest {
    private companion object {
        val logger = LoggerFactory.getLogger(MellomlagringTest::class.java)

        val redisServer: RedisServer = RedisServer
            .newRedisServer(6379)
            .started()

        val redisClient = RedisConfig.redisClient(
            redisHost = redisServer.host,
            redisPort = redisServer.bindPort
        )


        val redisStore = RedisStore(
            redisClient
        )

        val mellomlagringService = MellomlagringService(
            redisStore,
            "VerySecretPass"
        )

        @AfterClass
        @JvmStatic
        fun teardown() {
            redisClient.shutdown()
            redisServer.stop()
        }
    }

    @Test
    internal fun `mellomlagre verdier`() {
        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")

        assertEquals("test", mellomlagring)
    }

    @Test
    internal fun `verdier skal være krypterte`() {

        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")
        assertNotNull(redisStore.get("mellomlagring_test"))
        assertNotEquals(mellomlagring, redisStore.get("test"))
    }

    @Test
    internal fun `Oppdatering av mellomlagret verdi, skal ikke slette expiry`() {
        val key = "test"
        val expirationDate = Calendar.getInstance().let {
            it.add(Calendar.MINUTE, 1)
            it.time
        }

        mellomlagringService.setMellomlagring(
            fnr = key,
            midlertidigSøknad = "test",
            expirationDate = expirationDate
        )
        val verdi = mellomlagringService.getMellomlagring(key)
        assertEquals("test", verdi)
        val ttl = mellomlagringService.getTTLInMs(key)
        assertNotEquals(ttl, "-2")
        assertNotEquals(ttl, "-1")

        logger.info("PTTL=$ttl")

        mellomlagringService.updateMellomlagring(key, "test2")
        val oppdatertVerdi = mellomlagringService.getMellomlagring(key)
        assertEquals("test2", oppdatertVerdi)
        assertNotEquals(ttl, "-2")
        assertNotEquals(ttl, "-1")


    }

    @Test
    internal fun `mellomlagret verdier skal være utgått etter 500 ms`() {
        val fnr = "12345678910"
        val søknad = "test"

        val expirationDate = Calendar.getInstance().let {
            it.add(Calendar.MILLISECOND, 500)
            it.time
        }
        mellomlagringService.setMellomlagring(fnr, søknad, expirationDate = expirationDate)
        val forventetVerdi1 = mellomlagringService.getMellomlagring(fnr)
        logger.info("Hentet mellomlagret verdi = {}", forventetVerdi1)
        assertEquals("test", forventetVerdi1)
        assertNotEquals(mellomlagringService.getTTLInMs(fnr), "-2")
        assertNotEquals(mellomlagringService.getTTLInMs(fnr), "-1")

        Awaitility.waitAtMost(Durations.ONE_SECOND).untilAsserted {
            val forventetVerdi2 = mellomlagringService.getMellomlagring(fnr)
            logger.info("Hentet mellomlagret verdi = {}", forventetVerdi2)
            assertNull(forventetVerdi2)
        }
    }

}