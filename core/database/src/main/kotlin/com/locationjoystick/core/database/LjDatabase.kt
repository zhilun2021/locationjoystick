package com.locationjoystick.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.locationjoystick.core.database.dao.FavoriteDao
import com.locationjoystick.core.database.dao.RouteDao
import com.locationjoystick.core.database.dao.WaypointDao
import com.locationjoystick.core.database.entities.FavoriteEntity
import com.locationjoystick.core.database.entities.RouteEntity
import com.locationjoystick.core.database.entities.WaypointEntity

@Database(
    entities = [
        RouteEntity::class,
        WaypointEntity::class,
        FavoriteEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class LjDatabase : RoomDatabase() {

    abstract fun routeDao(): RouteDao

    abstract fun waypointDao(): WaypointDao

    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "locationjoystick.db"
    }
}
