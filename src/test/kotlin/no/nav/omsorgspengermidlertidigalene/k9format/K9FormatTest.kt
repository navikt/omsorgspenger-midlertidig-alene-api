package no.nav.omsorgspengermidlertidigalene.k9format

import no.nav.k9.søknad.JsonUtils
import no.nav.omsorgspengermidlertidigalene.SøknadUtils
import no.nav.omsorgspengermidlertidigalene.SøknadUtils.søker
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test

class K9FormatTest {

    @Test
    fun `Gyldig søknad blir til forventet k9-format`() {
        val mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknadId = UUID.randomUUID().toString()

        val søknad = SøknadUtils.gyldigSøknad.copy(søknadId = søknadId)
        val k9Format = søknad.tilK9Format(mottatt, søker)

        val forventetK9FormatJson =
            //language=json
            """
                {
                  "søknadId": $søknadId,
                  "versjon": "1.0.0",
                  "mottattDato": "2020-01-02T03:04:05.000Z",
                  "søker": {
                    "norskIdentitetsnummer": "02119970078"
                  },
                  "språk": "nb",
                  "ytelse": {
                    "type": "OMP_UTV_MA",
                    "barn": [
                      {
                        "norskIdentitetsnummer": "25058118020",
                        "fødselsdato": null
                      }
                    ],
                    "annenForelder": {
                      "norskIdentitetsnummer": "02119970078",
                      "situasjon": "FENGSEL",
                      "situasjonBeskrivelse": "Sitter i fengsel..",
                      "periode": "2020-01-01/2020-10-01"
                    },
                    "begrunnelse": null
                  },
                  "journalposter": []
                }
        """.trimIndent()

        JSONAssert.assertEquals(forventetK9FormatJson, JsonUtils.toString(k9Format), true)
    }

}