package com.vncoder.roomstudent.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vncoder.roomstudent.Entity.Student

@Database(entities = [Student::class],version = 1,exportSchema = false)
abstract class StudentDatabase : RoomDatabase(){
    abstract fun studentDao() : StudentDao?

    companion object{
        @Volatile
        private var INSTANCE : StudentDatabase?=null
        fun getData(context: Context): StudentDatabase?{
            val tempInstance =
                INSTANCE
            if (tempInstance!=null) {
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentDatabase::class.java,
                    "student_data"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}