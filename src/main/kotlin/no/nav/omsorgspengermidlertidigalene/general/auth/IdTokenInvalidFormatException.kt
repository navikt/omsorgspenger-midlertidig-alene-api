package no.nav.omsorgspengermidlertidigalene.general.auth

class IdTokenInvalidFormatException(idToken: IdToken, cause: Throwable? = null) : RuntimeException("$idToken er på ugyldig format.", cause)