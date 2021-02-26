package no.nav.omsorgspengermidlertidigalene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Utenlandsopphold
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.starterMedFodselsdato
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.valider
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertTrue

internal class SøknadValideringsTest {

    companion object {
        private val ugyldigFødselsnummer = "12345678900"
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
    fun `Feiler dersom antall barn er 0 eller mindre`(){
        SøknadUtils.gyldigSøknad.copy(
            antallBarn = 0
        ).valider()
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

    @Test(expected = Throwblem::class)
    fun `Feiler dersom annen forelder har ugyldig fnr`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            annenForelder = SøknadUtils.gyldigSøknad.annenForelder.copy(
                fnr = ugyldigFødselsnummer
            )
        )
        søknad.valider()
    }

    @Test(expected =  Throwblem::class)
    fun `Feiler dersom annen forelder sitt navn er ugydlig`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            annenForelder = SøknadUtils.gyldigSøknad.annenForelder.copy(
                navn = "   "
            )
        )
        søknad.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler dersom fødselsår på barn er høyere enn året vi er i`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            fødselsårBarn = listOf(
                LocalDate.now().year.plus(1)
            )
        )
        søknad.valider()
    }

}
