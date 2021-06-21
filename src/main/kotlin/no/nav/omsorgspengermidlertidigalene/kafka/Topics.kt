package no.nav.omsorgspengermidlertidigalene.kafka

import no.nav.omsorgspengermidlertidigalene.felles.Metadata
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer

data class TopicEntry<V>(
    val metadata: Metadata,
    val data: V
)

internal data class TopicUse<V>(
    val name: String,
    val valueSerializer : Serializer<TopicEntry<V>>
) {
    internal fun keySerializer() = StringSerializer()
}

object Topics {
    const val MOTTATT_OMS_MIDLERTIDIG_ALENE = "dusseldorf.privat-omsorgspenger-midlertidig-alene-mottatt"
}