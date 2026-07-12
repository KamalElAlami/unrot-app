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
data class ChallengeProgressEntity(val programId: String, val day: Int, val outcome: String, val completedAt: Long?, val note: String? = null)

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_runs ORDER BY epochDay DESC") fun observeRuns(): kotlinx.coroutines.flow.Flow<List<FocusRunEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveRun(run: FocusRunEntity)
    @Query("SELECT * FROM focus_runs WHERE epochDay = :epochDay AND practice = 0 LIMIT 1") suspend fun dailyRun(epochDay: Long): FocusRunEntity?
    @Query("SELECT COALESCE(SUM(durationMs), 0) FROM focus_runs WHERE epochDay = :epochDay AND practice = 1") suspend fun practiceDuration(epochDay: Long): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveProgress(progress: ChallengeProgressEntity)
    @Query("SELECT * FROM challenge_progress WHERE programId = :programId ORDER BY day") fun observeProgress(programId: String): kotlinx.coroutines.flow.Flow<List<ChallengeProgressEntity>>
    @Query("SELECT * FROM challenge_progress WHERE programId = :programId ORDER BY day") suspend fun progressForProgram(programId: String): List<ChallengeProgressEntity>
    @Query("SELECT * FROM challenge_progress WHERE programId = :programId AND day = :day LIMIT 1") suspend fun progress(programId: String, day: Int): ChallengeProgressEntity?
    @Query("DELETE FROM challenge_progress WHERE programId = :programId") suspend fun clearProgress(programId: String)
    @Query("UPDATE challenge_progress SET note = :note WHERE programId = :programId AND day = :day") suspend fun updateNote(programId: String, day: Int, note: String?)
    @Query("DELETE FROM focus_runs") suspend fun clearRuns()
    @Query("DELETE FROM challenge_progress") suspend fun clearAllProgress()
}

@Database(entities = [FocusRunEntity::class, ChallengeProgressEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao
    companion object {
        private val migration1To2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE challenge_progress ADD COLUMN note TEXT")
            }
        }
        fun create(context: android.content.Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "focus-reset.db")
            .addMigrations(migration1To2)
            .build()
    }
}
