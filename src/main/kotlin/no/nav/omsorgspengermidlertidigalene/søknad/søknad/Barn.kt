package no.nav.omsorgspengermidlertidigalene.søknad.søknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengermidlertidigalene.felles.gyldigNorskIdentifikator

data class Barn (
    val navn: String,
    val aktørId: String?,
    var identitetsnummer: String?,
) {
    fun manglerIdentitetsnummer(): Boolean = identitetsnummer.isNullOrEmpty()

    infix fun oppdaterIdentitetsnummerMed(identitetsnummer: String?){
        this.identitetsnummer = identitetsnummer
    }

    fun valider(index: Int): MutableSet<Violation> {
        val mangler: MutableSet<Violation> = mutableSetOf()

        if(identitetsnummer == null){
            mangler.add(
                Violation(
                    parameterName = "barn[$index].identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Barn.identitetsnummer kan ikke være null",
                    invalidValue = identitetsnummer
                )
            )
        }

        if(identitetsnummer != null && !identitetsnummer!!.gyldigNorskIdentifikator()){
            mangler.add(
                Violation(
                    parameterName = "barn[$index].identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Barn.identitetsnummer må være gyldig norsk identifikator",
                    invalidValue = identitetsnummer
                )
            )
        }

        if(navn.isNullOrBlank()){
            mangler.add(
                Violation(
                    parameterName = "barn[$index].navn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Barn.navn må kan ikke være null, tom eller bare mellomrom",
                    invalidValue = navn
                )
            )
        }

        return mangler
    }
}