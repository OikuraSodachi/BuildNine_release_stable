package com.todokanai.buildnine.di

import android.content.Context
import com.todokanai.buildnine.room.MyDatabase
import com.todokanai.buildnine.room.RoomPathDao
import com.todokanai.buildnine.room.RoomTrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideMyDatabase(@ApplicationContext context: Context) : MyDatabase {
        return MyDatabase.getInstance(context)
    }

    @Provides
    fun provideTrackDao(myDatabase: MyDatabase): RoomTrackDao {
        return myDatabase.roomTrackDao()
    }

    @Provides
    fun providePathDao(myDatabase: MyDatabase): RoomPathDao {
        return myDatabase.roomPathDao()
    }

}