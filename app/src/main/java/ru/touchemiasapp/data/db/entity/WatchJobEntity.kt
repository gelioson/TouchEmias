package ru.touchemiasapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "watch_jobs")
@TypeConverters(WatchJobConverters::class)
data class WatchJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val specialityId: Long,
    @ColumnInfo val specialityName: String,
    @ColumnInfo val doctorIds: List<Long>,
    @ColumnInfo val complexResourceIds: List<Long>,
    @ColumnInfo val doctorNames: List<String>,
    @ColumnInfo val clinicNames: List<String>,
    @ColumnInfo val selectedDates: List<String>,   // yyyy-MM-dd
    @ColumnInfo val timeFrom: String,              // HH:mm
    @ColumnInfo val timeTo: String,                // HH:mm
    @ColumnInfo val mode: String,                  // NOTIFY_ONLY | AUTO_BOOK
    @ColumnInfo val intervalSeconds: Int,
    @ColumnInfo val isActive: Boolean = false,
    @ColumnInfo val createdAt: Long = System.currentTimeMillis()
)

class WatchJobConverters {
    private val gson = Gson()

    @TypeConverter fun fromLongList(value: List<Long>): String = gson.toJson(value)
    @TypeConverter fun toLongList(value: String): List<Long> =
        gson.fromJson(value, object : TypeToken<List<Long>>() {}.type) ?: emptyList()

    @TypeConverter fun fromStringList(value: List<String>): String = gson.toJson(value)
    @TypeConverter fun toStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()
}
