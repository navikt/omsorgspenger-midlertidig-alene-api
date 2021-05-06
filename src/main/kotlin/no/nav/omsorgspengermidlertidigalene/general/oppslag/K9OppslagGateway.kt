package no.nav.omsorgspengermidlertidigalene.general.oppslag

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.omsorgspengermidlertidigalene.general.CallId
import no.nav.omsorgspengermidlertidigalene.general.auth.ApiGatewayApiKey
import no.nav.omsorgspengermidlertidigalene.general.auth.IdToken
import java.net.URI

abstract class K9OppslagGateway(
    protected val baseUrl: URI
): HealthCheck {

    protected fun generateHttpRequest(
        idToken: IdToken,
        url: String,
        callId: CallId
    ): Request {
        return url
            .httpGet()
            .header(
                HttpHeaders.Authorization to "Bearer ${idToken.value}",
                HttpHeaders.Accept to "application/json",
                HttpHeaders.XCorrelationId to callId.value
            )
    }
}
