package no.nav.omsorgspengermidlertidigalene.redis

import no.nav.omsorgspengermidlertidigalene.redis.RedisMockUtil.startRedisMocked

class RedisConfigurationProperties(private val redisMocked: Boolean) {

    fun startInMemoryRedisIfMocked() {
        if (redisMocked) {
            startRedisMocked()
        }
    }
}