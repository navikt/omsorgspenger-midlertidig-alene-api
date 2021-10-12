package no.nav.omsorgspengermidlertidigalene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgspengermidlertidigalene.felles.starterMedFodselsdato
import no.nav.omsorgspengermidlertidigalene.k9format.tilK9Format
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Barn
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.valider
import org.junit.jupiter.api.Assertions
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

internal class SøknadValideringsTest {

    companion object {
        private val ugyldigFødselsnummer = "12345678900"
        private val mottatt = ZonedDateTime.now()
    }

    @Test
    fun `Tester gyldig fødselsdato dersom dnunmer`() {
        val starterMedFodselsdato = "630293".starterMedFodselsdato()
        assertTrue(starterMedFodselsdato)
    }

    @Test
    fun `Gyldig søknad`() {
        val søknad = SøknadUtils.gyldigSøknad
        søknad.valider(søknad.tilK9Format(mottatt, SøknadUtils.søker))
    }

    @Test
    fun `Feiler dersom harForståttRettigheterOgPlikter er false`() {
        val søknad = SøknadUtils.gyldigSøknad.copy(
            harForståttRettigheterOgPlikter = false
        )
        Assertions.assertThrows(Throwblem::class.java) {
            søknad.valider(søknad.tilK9Format(mottatt, SøknadUtils.søker))
        }
    }

    @Test
    fun `Feiler dersom harBekreftetOpplysninger er false`() {
        val søknad = SøknadUtils.gyldigSøknad.copy(
            harBekreftetOpplysninger = false
        )
        Assertions.assertThrows(Throwblem::class.java) {
            søknad.valider(søknad.tilK9Format(mottatt, SøknadUtils.søker))
        }
    }

    @Test
    fun `Feiler dersom annen forelder har ugyldig fnr`() {
        val søknad = SøknadUtils.gyldigSøknad.copy(
            annenForelder = SøknadUtils.gyldigSøknad.annenForelder.copy(
                fnr = ugyldigFødselsnummer
            )
        )
        Assertions.assertThrows(Throwblem::class.java){
            søknad.valider(søknad.tilK9Format(mottatt, SøknadUtils.søker))
        }
    }

    @Test
    fun `Feiler dersom annen forelder sitt navn er ugydlig`() {
        val søknad = SøknadUtils.gyldigSøknad.copy(
            annenForelder = SøknadUtils.gyldigSøknad.annenForelder.copy(
                navn = "   "
            )
        )
        Assertions.assertThrows(Throwblem::class.java) {
            søknad.valider(søknad.tilK9Format(mottatt, SøknadUtils.søker))
        }
    }

    @Test
    fun `Feiler dersom barn ikke har identitetsnummer`() {
        val søknad = SøknadUtils.gyldigSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "Ole Dole Doffen",
                    aktørId = null,
                    identitetsnummer = null
                )
            )
        )
        Assertions.assertThrows(Throwblem::class.java) {
            søknad.valider(søknad.tilK9Format(mottatt, SøknadUtils.søker))
        }
    }

    @Test
    fun `Feiler dersom barn ikke har gyldig identitetsnummer`() {
        val søknad = SøknadUtils.gyldigSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "Ole Dole Doffen",
                    aktørId = null,
                    identitetsnummer = "ikke gyldig"
                )
            )
        )
        Assertions.assertThrows(Throwblem::class.java) {
            søknad.valider(søknad.tilK9Format(mottatt, SøknadUtils.søker))
        }
    }

    @Test
    fun `Feiler dersom barn ikke har navn`() {
        val søknad = SøknadUtils.gyldigSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "",
                    aktørId = "12345",
                    identitetsnummer = "12345"
                )
            )
        )
        Assertions.assertThrows(Throwblem::class.java) {
            søknad.valider(søknad.tilK9Format(mottatt, SøknadUtils.søker))
        }
    }

    @Test
    fun `Feiler dersom barn er tom liste`() {
        val søknad = SøknadUtils.gyldigSøknad.copy(
            barn = listOf()
        )
        Assertions.assertThrows(Throwblem::class.java) {
            søknad.valider(søknad.tilK9Format(mottatt, SøknadUtils.søker))
        }
    }

}
