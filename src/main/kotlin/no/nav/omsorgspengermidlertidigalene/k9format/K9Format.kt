package no.nav.omsorgspengermidlertidigalene.k9format

import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerMidlertidigAlene
import no.nav.omsorgspengermidlertidigalene.søker.Søker
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Barn
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Søknad
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

private val k9FormatVersjon = Versjon.of("1.0.0")


fun Søknad.tilK9Format(mottatt: ZonedDateTime, søker: Søker): K9Søknad {
    return K9Søknad(
        SøknadId.of(søknadId),
        k9FormatVersjon,
        mottatt,
        søker.tilK9Søker(),
        OmsorgspengerMidlertidigAlene(
            barn.tilK9Barn(),
            annenForelder.tilK9AnnenForelder(),
            null
        )
    )
}

fun Søker.tilK9Søker(): K9Søker = K9Søker(NorskIdentitetsnummer.of(fødselsnummer))

fun List<Barn>.tilK9Barn(): List<K9Barn> = map {
    K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(it.identitetsnummer))
}