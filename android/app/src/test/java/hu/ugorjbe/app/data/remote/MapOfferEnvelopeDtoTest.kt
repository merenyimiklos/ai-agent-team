package hu.ugorjbe.app.data.remote

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.ugorjbe.app.domain.MapBounds
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class MapOfferEnvelopeDtoTest {
    private val moshi = Moshi.Builder()
        .add(BigDecimal::class.java, BigDecimalJsonAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(MapOfferEnvelopeDto::class.java)

    @Test
    fun `real map envelope deserializes and keeps requested bounds`() {
        val json = """
            {
              "items": [
                {
                  "id": "33333333-3333-3333-3333-333333333333",
                  "provider": {
                    "id": "22222222-2222-2222-2222-222222222222",
                    "name": "Városi Alkotóműhely",
                    "shortDescription": "Kreatív programok családoknak",
                    "address": {
                      "postalCode": "1075",
                      "city": "Budapest",
                      "street": "Király utca 10.",
                      "countryCode": "HU",
                      "latitude": 47.4991,
                      "longitude": 19.0577
                    },
                    "imageUrl": null
                  },
                  "address": {
                    "postalCode": "1075",
                    "city": "Budapest",
                    "street": "Király utca 10.",
                    "countryCode": "HU",
                    "latitude": 47.4991,
                    "longitude": 19.0577
                  },
                  "title": "Délutáni agyagozás",
                  "category": "WORKSHOP",
                  "startsAtUtc": "2026-07-15T15:00:00Z",
                  "endsAtUtc": "2026-07-15T16:30:00Z",
                  "minChildAge": 5,
                  "maxChildAge": 12,
                  "originalUnitPrice": { "amount": 6000, "currency": "HUF" },
                  "discountedUnitPrice": { "amount": 3900, "currency": "HUF" },
                  "discountPercent": 35,
                  "availablePlaces": 8,
                  "distanceKm": 1.4,
                  "imageUrl": null
                }
              ],
              "isTruncated": false,
              "limit": 200
            }
        """.trimIndent()

        val dto = requireNotNull(adapter.fromJson(json))
        val requestedBounds = MapBounds(47.42, 18.92, 47.59, 19.18)
        val domain = dto.toDomain(requestedBounds)

        assertEquals(1, domain.returnedCount)
        assertEquals(200, domain.limit)
        assertFalse(domain.isTruncated)
        assertEquals(requestedBounds, domain.bounds)
        assertEquals("Délutáni agyagozás", domain.items.single().title)
        assertEquals(BigDecimal("3900"), domain.items.single().discountedUnitPrice.amount)
    }

    @Test
    fun `missing required map envelope field is a contract failure`() {
        val malformed = """
            {
              "items": [],
              "limit": 200
            }
        """.trimIndent()

        assertThrows(JsonDataException::class.java) {
            adapter.fromJson(malformed)
        }
    }
}
