package com.focusreset.app.data

import androidx.room.*

@Entity(tableName = "focus_runs")
data class FocusRunEntity(
    @PrimaryKey val id: String,
    val epochDay: Long,
    val seed: Long,
    val score: Int,
    val accuracy: Int,
    val consistency: Int,
    val impulseControl: Int,
    val durationMs: Long,
    val practice: Boolean
)

@Entity(tableName = "challenge_progress", primaryKeys = ["programId", "day"])
data class ChallengeProgressEntity(val programId: String, val day: Int, val outcome: String, val completedAt: Long?)

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_runs ORDER BY epochDay DESC") fun observeRuns(): kotlinx.coroutines.flow.Flow<List<FocusRunEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveRun(run: FocusRunEntity)
    @Query("SELECT * FROM focus_runs WHERE epochDay = :epochDay AND practice = 0 LIMIT 1") suspend fun dailyRun(epochDay: Long): FocusRunEntity?
    @Query("SELECT COALESCE(SUM(durationMs), 0) FROM focus_runs WHERE epochDay = :epochDay AND practice = 1") suspend fun practiceDuration(epochDay: Long): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveProgress(progress: ChallengeProgressEntity)
    @Query("SELECT * FROM challenge_progress WHERE programId = :programId ORDER BY day") fun observeProgress(programId: String): kotlinx.coroutines.flow.Flow<List<ChallengeProgressEntity>>
}

@Database(entities = [FocusRunEntity::class, ChallengeProgressEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao
    companion object {
        fun create(context: android.content.Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "focus-reset.db").build()
    }
}
