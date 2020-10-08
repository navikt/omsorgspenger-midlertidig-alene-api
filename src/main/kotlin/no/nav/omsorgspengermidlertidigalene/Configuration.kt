package no.nav.omsorgspengermidlertidigalene

import io.ktor.config.*
import io.ktor.util.*
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.omsorgspengermidlertidigalene.general.auth.ApiGatewayApiKey
import no.nav.omsorgspengermidlertidigalene.kafka.KafkaConfig
import java.net.URI

@KtorExperimentalAPI
data class Configuration(val config : ApplicationConfig) {
    internal fun getJwksUrl() = URI(config.getRequiredString("nav.authorization.jwks_uri", secret = false))

    internal fun getIssuer() : String {
        return config.getRequiredString("nav.authorization.issuer", secret = false)
    }

    internal fun getCookieName() : String {
        return config.getRequiredString("nav.authorization.cookie_name", secret = false)
    }

    internal fun getWhitelistedCorsAddreses() : List<URI> {
        return config.getOptionalList(
            key = "nav.cors.addresses",
            builder = { value ->
                URI.create(value)
            },
            secret = false
        )
    }

    internal fun getK9OppslagUrl() = URI(config.getRequiredString("nav.gateways.k9_oppslag_url", secret = false))

    internal fun getApiGatewayApiKey() : ApiGatewayApiKey {
        val apiKey = config.getRequiredString(key = "nav.authorization.api_gateway.api_key", secret = true)
        return ApiGatewayApiKey(value = apiKey)
    }

    internal fun getKafkaConfig() = config.getRequiredString("nav.kafka.bootstrap_servers", secret = false).let { bootstrapServers ->
        val trustStore = config.getOptionalString("nav.trust_store.path", secret = false)?.let { trustStorePath ->
            config.getOptionalString("nav.trust_store.password", secret = true)?.let { trustStorePassword ->
                Pair(trustStorePath, trustStorePassword)
            }
        }

        KafkaConfig(
            bootstrapServers = bootstrapServers,
            credentials = Pair(config.getRequiredString("nav.kafka.username", secret = false), config.getRequiredString("nav.kafka.password", secret = true)),
            trustStore = trustStore
        )
    }

    internal fun getRedisPort() = config.getOptionalString("nav.redis.port", secret = false)
    internal fun getRedisHost() = config.getOptionalString("nav.redis.host", secret = false)

    internal fun getStoragePassphrase() : String {
        return config.getRequiredString("nav.storage.passphrase", secret = true)
    }
}