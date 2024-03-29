package no.nav.omsorgspengermidlertidigalene

import com.github.fppt.jedismock.RedisServer
import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.common.KafkaEnvironment
import no.nav.helse.TestUtils.getAuthCookie
import no.nav.helse.TestUtils.getTokenDingsToken
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.omsorgspengermidlertidigalene.felles.*
import no.nav.omsorgspengermidlertidigalene.kafka.Topics
import no.nav.omsorgspengermidlertidigalene.mellomlagring.started
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.AnnenForelder
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Situasjon
import no.nav.omsorgspengermidlertidigalene.wiremock.omsorgspengerMidlertidigAleneApiConfig
import no.nav.omsorgspengermidlertidigalene.wiremock.stubK9OppslagBarn
import no.nav.omsorgspengermidlertidigalene.wiremock.stubK9OppslagSoker
import no.nav.omsorgspengermidlertidigalene.wiremock.stubOppslagHealth
import org.json.JSONObject
import org.junit.AfterClass
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue



class ApplicationTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(ApplicationTest::class.java)

        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .withTokendingsSupport()
            .omsorgspengerMidlertidigAleneApiConfig()
            .build()
            .stubOppslagHealth()
            .stubK9OppslagBarn()
            .stubK9OppslagSoker()

        val redisServer: RedisServer = RedisServer.newRedisServer().started()

        private const val gyldigFodselsnummerA = "290990123456"
        private const val gyldigFodselsnummerB = "25118921464"
        private const val ikkeMyndigFnr = "12125012345"

        private val kafkaEnvironment = KafkaWrapper.bootstrap()
        private val kafkaTestConsumer = kafkaEnvironment.testConsumer()
        val tokenXToken = getTokenDingsToken(fnr = gyldigFodselsnummerA)


        fun getConfig(kafkaEnvironment: KafkaEnvironment): ApplicationConfig {
            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(TestConfiguration.asMap(
                kafkaEnvironment = kafkaEnvironment,
                wireMockServer = wireMockServer,
                redisServer = redisServer
            ))
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }


        val engine = TestApplicationEngine(createTestEnvironment {
            config = getConfig(kafkaEnvironment)
        }).apply {
            start(wait = true)
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            redisServer.stop()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun `test isready, isalive, health og metrics`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/isalive") {}.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    handleRequest(HttpMethod.Get, "/metrics") {}.apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        handleRequest(HttpMethod.Get, "/health") {}.apply {
                            assertEquals(HttpStatusCode.OK, response.status())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `Hente søker med tokenx`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = SØKER_URL,
            expectedCode = HttpStatusCode.OK,
            jwtToken = tokenXToken,
            expectedResponse = """
                {
                  "aktørId": "12345",
                  "fødselsdato": "1997-05-25",
                  "fødselsnummer": "290990123456",
                  "fornavn": "MOR",
                  "mellomnavn": "HEISANN",
                  "etternavn": "MORSEN",
                  "myndig": true
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Hente søker`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = SØKER_URL,
            expectedCode = HttpStatusCode.OK,
            cookie = getAuthCookie(gyldigFodselsnummerA),
            expectedResponse = """
                {
                  "aktørId": "12345",
                  "fødselsdato": "1997-05-25",
                  "fødselsnummer": "290990123456",
                  "fornavn": "MOR",
                  "mellomnavn": "HEISANN",
                  "etternavn": "MORSEN",
                  "myndig": true
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Hente søker hvor man får 451 fra oppslag`() {
        wireMockServer.stubK9OppslagSoker(
            statusCode = HttpStatusCode.fromValue(451),
            responseBody =
            //language=json
            """
            {
                "detail": "Policy decision: DENY - Reason: (NAV-bruker er i live AND NAV-bruker er ikke myndig)",
                "instance": "/meg",
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451
            }
            """.trimIndent()
        )

        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = SØKER_URL,
            expectedCode = HttpStatusCode.fromValue(451),
            expectedResponse =
            //language=json
            """
            {
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451,
                "instance": "/soker",
                "detail": "Tilgang nektet."
            }
            """.trimIndent(),
            cookie = getAuthCookie(ikkeMyndigFnr)
        )

        wireMockServer.stubK9OppslagSoker()
    }

    @Test
    fun `Hente søker som ikke er myndig`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = SØKER_URL,
            expectedCode = HttpStatusCode.OK,
            cookie = getAuthCookie(ikkeMyndigFnr),
            expectedResponse = """
                {
                  "aktørId": "12345",
                  "fødselsdato": "2050-12-12",
                  "fødselsnummer": "12125012345",
                  "fornavn": "MOR",
                  "mellomnavn": "HEISANN",
                  "etternavn": "MORSEN",
                  "myndig": false
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Hente barn hvor man får 451 fra oppslag`(){
        wireMockServer.stubK9OppslagBarn(
            statusCode = HttpStatusCode.fromValue(451),
            responseBody =
            //language=json
            """
            {
                "detail": "Policy decision: DENY - Reason: (NAV-bruker er i live AND NAV-bruker er ikke myndig)",
                "instance": "/meg",
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451
            }
            """.trimIndent()
        )

        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.fromValue(451),
            expectedResponse =
            //language=json
            """
            {
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451,
                "instance": "/barn",
                "detail": "Tilgang nektet."
            }
            """.trimIndent(),
            cookie = getAuthCookie(ikkeMyndigFnr)
        )

        wireMockServer.stubK9OppslagBarn() // reset til default mapping
    }

    @Test
    fun `Hente barn og sjekk eksplisit at identitetsnummer ikke blir med ved get kall`(){

        val respons = requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.OK,
            cookie = getAuthCookie(gyldigFodselsnummerA),
            //language=json
            expectedResponse = """
                {
                  "barnOppslag": [
                    {
                      "fødselsdato": "2000-08-27",
                      "fornavn": "BARN",
                      "mellomnavn": "EN",
                      "etternavn": "BARNESEN",
                      "aktørId": "1000000000001"
                    },
                    {
                      "fødselsdato": "2001-04-10",
                      "fornavn": "BARN",
                      "mellomnavn": "TO",
                      "etternavn": "BARNESEN",
                      "aktørId": "1000000000002"
                    }
                  ]
                }
            """.trimIndent()
        )

        val responsSomJSONArray = JSONObject(respons).getJSONArray("barnOppslag")

        assertFalse(responsSomJSONArray.getJSONObject(0).has("identitetsnummer"))
        assertFalse(responsSomJSONArray.getJSONObject(1).has("identitetsnummer"))
    }

    @Test
    fun `Feil ved henting av barn skal returnere tom liste`() {
        wireMockServer.stubK9OppslagBarn(simulerFeil = true)
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "barnOppslag": []
            }
            """.trimIndent(),
            cookie = getAuthCookie(gyldigFodselsnummerB)
        )
        wireMockServer.stubK9OppslagBarn()
    }

    @Test
    fun `Sende gyldig melding til validering`(){
        val søknad = SøknadUtils.gyldigSøknad.somJson()

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = null,
            cookie = getAuthCookie(gyldigFodselsnummerA),
            expectedCode = HttpStatusCode.Accepted,
            requestEntity = søknad
        )
    }

    @Test
    fun `Sende ugyldig søknad til validering`() {
        val ugyldigSøknad = SøknadUtils.gyldigSøknad.copy(
            harBekreftetOpplysninger = false,
            harForståttRettigheterOgPlikter = false
        ).somJson()

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = VALIDERING_URL,
            cookie = getAuthCookie(gyldigFodselsnummerA),
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "harBekreftetOpplysninger",
                      "reason": "Opplysningene må bekreftes for å sende inn søknad.",
                      "invalid_value": false
                    },
                    {
                      "type": "entity",
                      "name": "harForståttRettigheterOgPlikter",
                      "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                      "invalid_value": false
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            requestEntity = ugyldigSøknad
        )
    }

    @Test
    fun `Sende gyldig søknad og plukke opp fra kafka topic`() {
        val søknadID = UUID.randomUUID().toString()
        val søknad = SøknadUtils.gyldigSøknad.copy(søknadId = søknadID).somJson()

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            cookie = getAuthCookie(gyldigFodselsnummerA),
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            requestEntity = søknad
        )

        val søknadSendtTilProsessering = hentSøknadSendtTilProsessering(søknadID)
        verifiserAtInnholdetErLikt(JSONObject(søknad), søknadSendtTilProsessering)
    }

    @Test
    fun `Sende gyldig søknad med tokenX og plukke opp fra kafka topic`() {
        val søknadID = UUID.randomUUID().toString()
        val søknad = SøknadUtils.gyldigSøknad.copy(søknadId = søknadID).somJson()

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            jwtToken = tokenXToken,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            requestEntity = søknad
        )

        val søknadSendtTilProsessering = hentSøknadSendtTilProsessering(søknadID)
        verifiserAtInnholdetErLikt(JSONObject(søknad), søknadSendtTilProsessering)
    }

    @Test
    fun `Sende søknad hvor søker ikke er myndig`() {
        val cookie = getAuthCookie(ikkeMyndigFnr)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = """
                {
                    "type": "/problem-details/unauthorized",
                    "title": "unauthorized",
                    "status": 403,
                    "detail": "Søkeren er ikke myndig og kan ikke sende inn søknaden.",
                    "instance": "about:blank"
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.Forbidden,
            cookie = cookie,
            requestEntity = SøknadUtils.gyldigSøknad.somJson()
        )
    }

    @Test
    fun `Sende søknad som inneholder annenForelder som er ugyldig`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            annenForelder = AnnenForelder(
                navn = "",
                fnr = "0-0",
                situasjon = Situasjon.SYKDOM,
                situasjonBeskrivelse = " ",
                periodeOver6Måneder = false,
                periodeFraOgMed = LocalDate.parse("2021-01-01")
            )
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            cookie = getAuthCookie(gyldigFodselsnummerA),
            expectedResponse = """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                    {
                  "type": "entity",
                  "name": "AnnenForelder.navn",
                  "reason": "Navn på annen forelder kan ikke være null, tom eller kun white spaces",
                  "invalid_value": ""
                },
                {
                  "type": "entity",
                  "name": "AnnenForelder.fnr",
                  "reason": "Fødselsnummer på annen forelder må være gyldig norsk identifikator",
                  "invalid_value": "0-0"
                },
                {
                  "type": "entity",
                  "name": "AnnenForelder.situasjonBeskrivelse",
                  "reason": "Situasjonsbeskrivelse på annenForelder kan ikke være null, tom eller kun white spaces når situasjon er SYKDOM",
                  "invalid_value": " "
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            requestEntity = søknad.somJson()
        )
    }

    private fun requestAndAssert(
        httpMethod: HttpMethod,
        path: String,
        requestEntity: String? = null,
        expectedResponse: String?,
        expectedCode: HttpStatusCode,
        jwtToken: String? = null,
        cookie: Cookie? = null
    ): String? {
        val respons: String?
        with(engine) {
            handleRequest(httpMethod, path) {
                if (cookie != null) addHeader(HttpHeaders.Cookie, cookie.toString())
                if (jwtToken != null) addHeader(HttpHeaders.Authorization, "Bearer $jwtToken")
                logger.info("Request Entity = $requestEntity")
                addHeader(HttpHeaders.Accept, "application/json")
                if (requestEntity != null) addHeader(HttpHeaders.ContentType, "application/json")
                if (requestEntity != null) setBody(requestEntity)
            }.apply {
                logger.info("Response Entity = ${response.content}")
                logger.info("Expected Entity = $expectedResponse")
                respons = response.content
                assertEquals(expectedCode, response.status())
                if (expectedResponse != null) {
                    JSONAssert.assertEquals(expectedResponse, response.content!!, true)
                } else {
                    assertEquals(expectedResponse, response.content)
                }
            }
        }
        return respons
    }

    private fun hentSøknadSendtTilProsessering(soknadId: String): JSONObject {
        return kafkaTestConsumer.hentSøknad(soknadId, topic = Topics.MOTTATT_OMS_MIDLERTIDIG_ALENE).data
    }

    private fun verifiserAtInnholdetErLikt(
        søknadSendtInn: JSONObject,
        søknadPlukketFraTopic: JSONObject
    ) {
        assertTrue(søknadPlukketFraTopic.has("søker"))
        søknadPlukketFraTopic.remove("søker") //Fjerner søker siden det settes i komplettSøknad

        assertTrue(søknadPlukketFraTopic.has("mottatt"))
        søknadPlukketFraTopic.remove("mottatt") //Fjerner mottatt siden det settes i komplettSøknad

        søknadPlukketFraTopic.remove("k9Format") //Fjerner k9Format

        JSONAssert.assertEquals(søknadSendtInn, søknadPlukketFraTopic,  true)

        logger.info("Verifisering OK")
    }
}
