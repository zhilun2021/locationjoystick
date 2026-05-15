package com.locationjoystick.core.data

import app.cash.turbine.test
import com.locationjoystick.core.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRepositoryTest {
    private val repository = LocationRepository()

    @Test
    fun `setPositionInternal emits new position via observePosition`() =
        runTest {
            val position = LatLng(10.0, 20.0)

            repository.observePosition().test {
                // consume initial null emission
                awaitItem()

                repository.setPositionInternal(position)

                assertEquals(position, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setPositionInternal preserves exact coordinates`() =
        runTest {
            val position = LatLng(48.8566, 2.3522)

            repository.observePosition().test {
                awaitItem() // initial
                repository.setPositionInternal(position)
                val emitted = awaitItem()
                org.junit.Assert.assertNotNull(emitted)

                assertEquals(position.latitude, emitted!!.latitude, 0.00001)
                assertEquals(position.longitude, emitted.longitude, 0.00001)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setPositionInternal multiple updates emit all values`() =
        runTest {
            val pos1 = LatLng(0.0, 0.0)
            val pos2 = LatLng(1.0, 1.0)
            val pos3 = LatLng(2.0, 2.0)

            repository.observePosition().test {
                awaitItem() // initial
                repository.setPositionInternal(pos1)
                assertEquals(pos1, awaitItem())

                repository.setPositionInternal(pos2)
                assertEquals(pos2, awaitItem())

                repository.setPositionInternal(pos3)
                assertEquals(pos3, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setPositionInternal different positions are distinct`() =
        runTest {
            val pos1 = LatLng(0.0, 0.0)
            val pos2 = LatLng(0.00001, 0.0) // ~1.1m away

            repository.observePosition().test {
                awaitItem()
                repository.setPositionInternal(pos1)
                val first = awaitItem()

                repository.setPositionInternal(pos2)
                val second = awaitItem()

                assertEquals(first, pos1)
                assertEquals(second, pos2)
                assertTrue("should be different positions", first != second)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
