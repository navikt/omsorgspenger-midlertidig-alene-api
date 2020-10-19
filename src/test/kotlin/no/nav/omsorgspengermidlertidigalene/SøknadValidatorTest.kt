package no.nav.omsorgspengermidlertidigalene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Utenlandsopphold
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.starterMedFodselsdato
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.valider
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertTrue


internal class SøknadValidatorTest {

    companion object {
        private val gyldigFødselsnummerA = "02119970078"
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
    fun `Feiler dersom antall barn er 0 eller mindre`(){
        SøknadUtils.gyldigSøknad.copy(
            antallBarn = 0
        ).valider()
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
    fun `Feiler dersom alder på barn er -1`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            alderAvAlleBarn = listOf(1,2,-1)
        )
        søknad.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler dersom medlemskap har et Utenlandsopphold som har ugydlig landnavn`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            medlemskap = SøknadUtils.gyldigSøknad.medlemskap.copy(
                utenlandsoppholdNeste12Mnd = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1),
                        landnavn = "  ",
                        landkode = "GE"
                    )
                )
            )
        )
        søknad.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler dersom medlemskap har et Utenlandsopphold som har ugydlig landkode`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            medlemskap = SøknadUtils.gyldigSøknad.medlemskap.copy(
                utenlandsoppholdNeste12Mnd = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1),
                        landnavn = "Sverige",
                        landkode = " "
                    )
                )
            )
        )
        søknad.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler dersom medlemskap har et Utenlandsopphold hvor fraOgMed er etter tilOgMed`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            medlemskap = SøknadUtils.gyldigSøknad.medlemskap.copy(
                utenlandsoppholdNeste12Mnd = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().minusDays(1),
                        landnavn = "Sverige",
                        landkode = "SWE"
                    )
                )
            )
        )
        søknad.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler dersom utenlandsoppholdIPerioden skalOppholdeSegIUtlandetIPerioden er true med opphold er tom`(){
        val søknad = SøknadUtils.gyldigSøknad.copy(
            utenlandsoppholdIPerioden = SøknadUtils.gyldigSøknad.utenlandsoppholdIPerioden.copy(
                skalOppholdeSegIUtlandetIPerioden = true,
                opphold = listOf()
                )
            )
        søknad.valider()
    }

}
