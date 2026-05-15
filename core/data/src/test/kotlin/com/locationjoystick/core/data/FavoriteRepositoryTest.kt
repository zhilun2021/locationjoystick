package com.locationjoystick.core.data

import app.cash.turbine.test
import com.locationjoystick.core.model.FavoriteLocation
import com.locationjoystick.core.model.LatLng
import com.locationjoystick.core.testing.FakeFavoriteDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryTest {
    private lateinit var dao: FakeFavoriteDao
    private lateinit var repository: FavoriteRepository

    @Before
    fun setUp() {
        dao = FakeFavoriteDao()
        repository = FavoriteRepository(dao)
    }

    @Test
    fun `getFavorites emits empty list initially`() =
        runTest {
            repository.getFavorites().test {
                assertTrue(awaitItem().isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `addFavorite then getFavorites emits added item`() =
        runTest {
            repository.getFavorites().test {
                awaitItem() // empty list

                repository.addFavorite(
                    id = "fav-1",
                    name = "Eiffel Tower",
                    position = LatLng(48.8584, 2.2945),
                    createdAt = 1_000L,
                )

                val list = awaitItem()
                assertEquals(1, list.size)
                assertEquals("Eiffel Tower", list.first().name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `deleteFavorite removes item from flow`() =
        runTest {
            repository.addFavorite("fav-1", "Tower", LatLng(48.8584, 2.2945), createdAt = 1_000L)

            repository.getFavorites().test {
                val initial = awaitItem()
                assertEquals(1, initial.size)

                repository.deleteFavorite("fav-1")

                val afterDelete = awaitItem()
                assertTrue(afterDelete.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `deleteFavorite non-existent id succeeds without error`() =
        runTest {
            val result = repository.deleteFavorite("does-not-exist")
            assertTrue(result.isSuccess)
        }

    @Test
    fun `getFavorites sorts by createdAt descending`() =
        runTest {
            repository.addFavorite("a", "Old", LatLng(0.0, 0.0), createdAt = 100L)
            repository.addFavorite("b", "New", LatLng(1.0, 0.0), createdAt = 200L)

            repository.getFavorites().test {
                val list = awaitItem()
                assertEquals(2, list.size)
                assertEquals("New", list.first().name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `updateFavorite changes name in flow`() =
        runTest {
            repository.addFavorite("fav-1", "Old Name", LatLng(0.0, 0.0), createdAt = 1_000L)

            val updated = FavoriteLocation(id = "fav-1", name = "New Name", position = LatLng(0.0, 0.0), createdAt = 1_000L)
            repository.updateFavorite(updated)

            repository.getFavorites().test {
                val list = awaitItem()
                assertEquals("New Name", list.first().name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `addFavorite preserves exact position`() =
        runTest {
            val position = LatLng(48.8584, 2.2945)
            repository.addFavorite("fav-1", "Tower", position, createdAt = 1_000L)

            repository.getFavorites().test {
                val list = awaitItem()
                assertEquals(position.latitude, list.first().position.latitude, 0.00001)
                assertEquals(position.longitude, list.first().position.longitude, 0.00001)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `updateFavorite preserves position`() =
        runTest {
            val originalPos = LatLng(0.0, 0.0)
            repository.addFavorite("fav-1", "A", originalPos, createdAt = 1_000L)

            val newPos = LatLng(51.5, -0.1)
            val updated = FavoriteLocation(id = "fav-1", name = "B", position = newPos, createdAt = 1_000L)
            repository.updateFavorite(updated)

            repository.getFavorites().test {
                val list = awaitItem()
                assertEquals(newPos.latitude, list.first().position.latitude, 0.00001)
                assertEquals(newPos.longitude, list.first().position.longitude, 0.00001)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `multiple favorites maintain creation order`() =
        runTest {
            repository.addFavorite("a", "First", LatLng(0.0, 0.0), createdAt = 100L)
            repository.addFavorite("b", "Second", LatLng(1.0, 0.0), createdAt = 200L)
            repository.addFavorite("c", "Third", LatLng(2.0, 0.0), createdAt = 300L)

            repository.getFavorites().test {
                val list = awaitItem()
                assertEquals(3, list.size)
                // Descending order (newest first)
                assertEquals("Third", list[0].name)
                assertEquals("Second", list[1].name)
                assertEquals("First", list[2].name)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
