package no.nav.omsorgspengermidlertidigalene

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.AnnenForelder
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Situasjon
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.valider
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class AnnenForelderValidatorTest {

    private companion object{
        val gyldigFødselsnummer = "26104500284"
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon SYKDOM `(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.SYKDOM,
            situasjonBeskrivelse = "COVID-19",
            periodeOver6Måneder = true
        )

        annenForelder.valider().assertAntallMangler(0)
    }

    @Test
    fun `Ved situasjon SYKDOM skal det gi feil dersom situasjonBeskrivelse er tom`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.SYKDOM,
            situasjonBeskrivelse = "",
            periodeOver6Måneder = true
        )
        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon SYKDOM skal det gi feil dersom periodeOver6Måneder ikke er satt`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.SYKDOM,
            situasjonBeskrivelse = "COVID-19"
        )
        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon INNLAGT_I_HELSEINSTITUSJON hvor periodeOver6Måneder er satt`() {
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeOver6Måneder = true
        )

        annenForelder.valider().assertAntallMangler(0)
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon INNLAGT_I_HELSEINSTITUSJON hvor periodeFraOgMed og periodeTilOgMed er satt`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-07-01")
        )

        annenForelder.valider().assertAntallMangler(0)
    }

    @Test
    fun `Ved situasjon INNLAGT_I_HELSEINSTITUSJON skal det gi feil dersom periodeOver6Måneder, periodeFraOgMed og periodeTilOgMed er null`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon INNLAGT_I_HELSEINSTITUSJON skal det gi feil dersom periodeOver6Måneder er false`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeOver6Måneder = false
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon INNLAGT_I_HELSEINSTITUSJON skal det gi feil dersom periodeTilOgMed er null`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = null
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon INNLAGT_I_HELSEINSTITUSJON skal det gi feil dersom periodeFraOgMed er null`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeFraOgMed = null,
            periodeTilOgMed = LocalDate.parse("2020-01-01")
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon INNLAGT_I_HELSEINSTITUSJON skal det gi feil dersom periodeTilOgMed er før periodeFraOgMed`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-01-01").minusDays(1)
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon FENGSEL`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-01-01")
        )

        annenForelder.valider().assertAntallMangler(0)
    }

    @Test
    fun `Ved situasjon FENGSEL skal det gi feil dersom periodeFraOgMed er null`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = null,
            periodeTilOgMed = LocalDate.parse("2021-01-01")
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon FENGSEL skal det gi feil dersom periodeTilOgMed er null`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = null
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon FENGSEL skal det gi feil dersom periodeTilOgMed er før periodeFraOgMed`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-01-01").minusDays(1)
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon UTØVER_VERNEPLIKT`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.UTØVER_VERNEPLIKT,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-01-01")
        )

        annenForelder.valider().assertAntallMangler(0)
    }

    @Test
    fun `Ved situasjon UTØVER_VERNEPLIKT skal det gi feil dersom periodeFraOgMed er null`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.UTØVER_VERNEPLIKT,
            periodeFraOgMed = null,
            periodeTilOgMed = LocalDate.parse("2021-01-01")
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon UTØVER_VERNEPLIKT skal det gi feil dersom periodeTilOgMed er null`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.UTØVER_VERNEPLIKT,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = null
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon UTØVER_VERNEPLIKT skal det gi feil dersom periodeTilOgMed er før periodeFraOgMed`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.UTØVER_VERNEPLIKT,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-01-01").minusDays(1)
        )

        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon ANNET `(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.ANNET,
            situasjonBeskrivelse = "Blabla noe skjedde",
            periodeOver6Måneder = true
        )

        annenForelder.valider().assertAntallMangler(0)
    }

    @Test
    fun `Ved situasjon ANNET skal det gi feil dersom situasjonBeskrivelse er tom`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.ANNET,
            situasjonBeskrivelse = "",
            periodeOver6Måneder = true
        )
        annenForelder.valider().assertAntallMangler(1)
    }

    @Test
    fun `Ved situasjon ANNET skal det gi feil dersom periodeOver6Måneder ikke er satt`(){
        val annenForelder = AnnenForelder(
            navn = "Kjell",
            fnr = gyldigFødselsnummer,
            situasjon = Situasjon.ANNET,
            situasjonBeskrivelse = "COVID-19"
        )
        annenForelder.valider().assertAntallMangler(1)
    }
}

private fun MutableSet<Violation>.assertAntallMangler(forventetAntallFeil: Int) {
    assertEquals(forventetAntallFeil, this.size)
}
