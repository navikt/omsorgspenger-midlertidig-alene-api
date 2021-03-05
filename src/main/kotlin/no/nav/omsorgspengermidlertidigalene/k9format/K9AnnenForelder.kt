package no.nav.omsorgspengermidlertidigalene.k9format

import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.AnnenForelder
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.Situasjon

fun AnnenForelder.tilK9AnnenForelder(): no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder =
    no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder(
        NorskIdentitetsnummer.of(fnr),
        situasjon.tilK9AnnenForelderSituasjon(),
        situasjonBeskrivelse,
        this.tilK9Periode()
    )

fun AnnenForelder.tilK9Periode(): Periode? {
    return if (periodeFraOgMed != null && periodeTilOgMed != null) {
        Periode(periodeFraOgMed, periodeTilOgMed)
    } else null
}

fun Situasjon.tilK9AnnenForelderSituasjon(): no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder.SituasjonType = when (this) {
    Situasjon.INNLAGT_I_HELSEINSTITUSJON -> no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder.SituasjonType.INNLAGT_I_HELSEINSTITUSJON
    Situasjon.UTØVER_VERNEPLIKT -> no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder.SituasjonType.UTØVER_VERNEPLIKT
    Situasjon.FENGSEL -> no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder.SituasjonType.FENGSEL
    Situasjon.SYKDOM -> no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder.SituasjonType.SYKDOM
    Situasjon.ANNET -> no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder.SituasjonType.ANNET
}