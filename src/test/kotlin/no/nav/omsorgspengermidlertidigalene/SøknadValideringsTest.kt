package no.nav.omsorgspengermidlertidigalene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgspengermidlertidigalene.felles.starterMedFodselsdato
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Barn
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.valider
import org.junit.Ignore
import org.junit.Test
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

    @Test(expected =  Throwblem::class)
    fun `Feiler dersom barn ikke har identitetsnummer`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "Ole Dole Doffen",
                    aktørId = null,
                    identitetsnummer = null
                )
            )
        )
        søknad.valider()
    }

    @Test(expected =  Throwblem::class)
    fun `Feiler dersom barn ikke har gyldig identitetsnummer`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "Ole Dole Doffen",
                    aktørId = null,
                    identitetsnummer = "ikke gyldig"
                )
            )
        )
        søknad.valider()
    }

    @Test(expected =  Throwblem::class)
    fun `Feiler dersom barn ikke har navn`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "",
                    aktørId = "12345",
                    identitetsnummer = "12345"
                )
            )
        )
        søknad.valider()
    }

    @Ignore //TODO 26.02.2021 - Skru på når frontend er prodsatt
    @Test(expected =  Throwblem::class)
    fun `Feiler dersom barn er tom liste`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            barn = listOf()
        )
        søknad.valider()
    }

}
