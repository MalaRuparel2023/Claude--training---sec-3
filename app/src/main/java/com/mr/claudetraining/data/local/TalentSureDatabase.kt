package com.mr.claudetraining.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Local store backing offline cache (jobs, users, messages, search results) and the draft
 * outbox. DAOs are added per feature as the offline layer lands; the schema is validated at
 * build time from the entities registered here.
 */
@Database(
    entities = [
        JobEntity::class,
        MessageEntity::class,
        UserEntity::class,
        JobQueryEntity::class,
        MessageDraftEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TalentSureDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun jobQueryDao(): JobQueryDao
    abstract fun messageDraftDao(): MessageDraftDao
}