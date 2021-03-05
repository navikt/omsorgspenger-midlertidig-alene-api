package no.nav.omsorgspengermidlertidigalene

import no.nav.omsorgspengermidlertidigalene.søker.Søker
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.AnnenForelder
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Barn
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Situasjon
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Søknad
import java.time.LocalDate

object SøknadUtils {

    val søker = Søker(
        aktørId = "12345",
        fødselsdato = LocalDate.parse("2000-01-01"),
        fødselsnummer = "02119970078",
        fornavn = "Ole",
        mellomnavn = "Dole",
        etternavn = "Doffen"
    )

    val gyldigSøknad = Søknad(
        id = "123456789",
        språk = "nb",
        annenForelder = AnnenForelder(
            navn = "Berit",
            fnr = "02119970078",
            situasjon = Situasjon.FENGSEL,
            situasjonBeskrivelse = "Sitter i fengsel..",
            periodeOver6Måneder = false,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-10-01")
        ),
        barn = listOf(
            Barn(
                navn = "Ole Dole",
                identitetsnummer = "25058118020",
                aktørId = null
            )
        ),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )
}