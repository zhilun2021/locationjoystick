package com.locationjoystick.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.locationjoystick.core.model.FavoriteLocation
import com.locationjoystick.core.model.LatLng

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long,
    @ColumnInfo(name = "category")
    val category: String? = null,
)

fun FavoriteEntity.toDomain(): FavoriteLocation =
    FavoriteLocation(
        id = id,
        name = name,
        position = LatLng(latitude = latitude, longitude = longitude),
        createdAt = createdAt,
        category = category,
    )

fun FavoriteLocation.toEntity(): FavoriteEntity =
    FavoriteEntity(
        id = id,
        name = name,
        latitude = position.latitude,
        longitude = position.longitude,
        createdAt = createdAt,
        category = category,
    )
