package no.nav.omsorgspengermidlertidigalene.redis

import io.ktor.util.*
import io.lettuce.core.RedisClient
import no.nav.omsorgspengermidlertidigalene.Configuration

internal object RedisConfig {

    @KtorExperimentalAPI
    internal fun redisClient(redisHost: String, redisPort: Int): RedisClient {
        return RedisClient.create("redis://${redisHost}:${redisPort}")
    }

}