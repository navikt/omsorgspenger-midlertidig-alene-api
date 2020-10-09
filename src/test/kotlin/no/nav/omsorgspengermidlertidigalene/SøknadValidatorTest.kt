package no.nav.omsorgspengermidlertidigalene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgspengermidlertidigalene.søknad.starterMedFodselsdato
import no.nav.omsorgspengermidlertidigalene.søknad.valider
import org.junit.Test
import kotlin.test.assertTrue


internal class SøknadValidatorTest {

    companion object {
        private val gyldigFodselsnummerA = "02119970078"
        private val gyldigFodselsnummerB = "19066672169"
        private val gyldigFodselsnummerC = "20037473937"
        private val dNummerA = "55125314561"
    }

    @Test
    fun `Tester gyldig fødselsdato dersom dnunmer`() {
        val starterMedFodselsdato = "630293".starterMedFodselsdato()
        assertTrue(starterMedFodselsdato)
    }

    @Test
    fun `Gyldig søknad`() {
        val søknad = SøknadUtils.gyldigSøknad
        søknad.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler dersom harForståttRettigheterOgPlikter er false`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            harForståttRettigheterOgPlikter = false
        )
        søknad.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler dersom harBekreftetOpplysninger er false`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            harBekreftetOpplysninger = false
        )
        søknad.valider()
    }

}
