package com.todokanai.buildnine.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RoomTrack::class,RoomPlayer::class,RoomPath::class,RoomNumber::class], version = 1, exportSchema = false)    // arrayOf(테이블(행렬)의 갯수), 버전
abstract class MyDatabase : RoomDatabase(){     // 각 테이블마다 Dao를 부르는 중간과정역할

    abstract fun roomTrackDao():RoomTrackDao

    abstract fun roomPlayerDao():RoomPlayerDao

    abstract fun roomPathDao():RoomPathDao

    abstract fun roomNumberDao():RoomNumberDao

    companion object {
        private var instance: MyDatabase? = null

        @Synchronized
        fun getInstance(context: Context): MyDatabase {
            if (instance == null) {
                synchronized(MyDatabase::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MyDatabase::class.java,
                        "room_db",
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance!!
        }
    }
}