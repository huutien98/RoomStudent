package com.vncoder.roomstudent.Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "student_table")
data class Student(
    @PrimaryKey(autoGenerate = true) var id:Int?=null,
    @ColumnInfo(name = "avatar") var avatar: String,
    @ColumnInfo(name = "masv") var masv:String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "birthday") var birthday: String,
    @ColumnInfo(name = "gender") var gender: String,
    @ColumnInfo(name = "address") var address: String,
    @ColumnInfo(name = "specialized") var specialized: String
):Serializable