package no.nav.omsorgspengermidlertidigalene.mellomlagring

import com.github.fppt.jedismock.RedisServer

internal fun RedisServer.started() = apply { start() }