package no.nav.omsorgspengermidlertidigalene.k9format

import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder.SituasjonType
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
    return if (periodeTilOgMed != null) {
        Periode(periodeFraOgMed, periodeTilOgMed)
    } else null
}

fun Situasjon.tilK9AnnenForelderSituasjon(): SituasjonType = when (this) {
    Situasjon.INNLAGT_I_HELSEINSTITUSJON -> SituasjonType.INNLAGT_I_HELSEINSTITUSJON
    Situasjon.UTØVER_VERNEPLIKT -> SituasjonType.UTØVER_VERNEPLIKT
    Situasjon.FENGSEL -> SituasjonType.FENGSEL
    Situasjon.SYKDOM -> SituasjonType.SYKDOM
    Situasjon.ANNET -> SituasjonType.ANNET
}