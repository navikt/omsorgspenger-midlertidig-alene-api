package no.nav.omsorgspengermidlertidigalene

import no.nav.omsorgspengermidlertidigalene.søknad.Søknad

class SøknadUtils {
    companion object {

        val gyldigSøknad = Søknad(
            språk = "nb",
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        )

    }
}
