package com.vncoder.roomstudent.Data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.vncoder.roomstudent.Entity.Student

@Dao
interface StudentDao {

    @Insert
    suspend fun insert(student: Student)

    @Query("SELECT * FROM student_table")
    suspend fun getAllPerson(): List<Student>

    @Delete
    suspend fun delete(student: Student)

    @Query("DELETE FROM student_table")
    suspend fun deleteAll()

    @Query("UPDATE student_table SET avatar = :avatar ,name = :name, birthday = :birthday, gender = :gender, address = :address, specialized =:specialized WHERE masv=:masv")
    suspend fun update(
        masv:String,
        avatar: String,
        name: String,
        birthday: String,
        gender: String,
        address: String,
        specialized: String
    )

    @Query("select * from student_table where name like '%'|| :name ||'%' or masv like '%'|| :name ||'%' " )
    suspend fun findByFirstName(name: String): List<Student>

    @Query("select * from student_table order by name ASC")
    suspend fun sortName(): List<Student>

    @Query("select masv from student_table")
    suspend fun queryCode(): List<String>

}