package no.nav.omsorgspengermidlertidigalene.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.*
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder

internal const val k9OppslagPath = "/helse-reverse-proxy/k9-selvbetjening-oppslag-mock"

internal fun WireMockBuilder.omsorgspengerMidlertidigAleneApiConfig() = wireMockConfiguration {
    it
        .extensions(SokerResponseTransformer())
        .extensions(BarnResponseTransformer())
}


internal fun WireMockServer.stubK9OppslagSoker() : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("aktør_id"))
            .withQueryParam("a", equalTo("fornavn"))
            .withQueryParam("a", equalTo("mellomnavn"))
            .withQueryParam("a", equalTo("etternavn"))
            .withQueryParam("a", equalTo("fødselsdato"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withTransformers("k9-oppslag-soker")
            )
    )
    return this
}

internal fun WireMockServer.stubK9OppslagBarn(simulerFeil: Boolean = false) : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("barn[].aktør_id"))
            .withQueryParam("a", equalTo("barn[].fornavn"))
            .withQueryParam("a", equalTo("barn[].mellomnavn"))
            .withQueryParam("a", equalTo("barn[].etternavn"))
            .withQueryParam("a", equalTo("barn[].fødselsdato"))
            .withQueryParam("a", equalTo("barn[].identitetsnummer"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(if (simulerFeil) 500 else 200)
                    .withTransformers("k9-oppslag-barn")
            )
    )
    return this
}

private fun WireMockServer.stubHealthEndpointThroughZones(
    path : String
) : WireMockServer{
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$path"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .willReturn(
            WireMock.aResponse()
                .withStatus(200)
        )
    )
    return this
}

internal fun WireMockServer.stubOppslagHealth() = stubHealthEndpointThroughZones("$k9OppslagPath/isalive")

internal fun WireMockServer.getK9OppslagUrl() = baseUrl() + k9OppslagPath