package com.vncoder.roomstudent.Adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vncoder.roomstudent.Entity.Student
import com.vncoder.roomstudent.R
import java.io.ByteArrayInputStream

class StudentAdapter (): RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    private lateinit var  ListStudent: MutableList<Student>
    private lateinit var listener: OnItemClickListener
    private val selected_items: SparseBooleanArray? = null
    private val current_selected_idx = -1

    constructor(listener: OnItemClickListener,ListStudent: MutableList<Student>):this(){
        this.listener =listener
        this.ListStudent = ListStudent
        setHasStableIds(true)
    }


    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        val img_avatar = itemView.findViewById<ImageView>(R.id.img_avatar)
        val tv_masv = itemView.findViewById<TextView>(R.id.tv_masv)
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val tv_birthday = itemView.findViewById<TextView>(R.id.tv_birthday)
        val tv_address = itemView.findViewById<TextView>(R.id.tv_address)
        val tv_gender = itemView.findViewById<TextView>(R.id.tv_gender)
        val tv_specialized = itemView.findViewById<TextView>(R.id.tv_skill)

        fun bindView(student: Student,listener: OnItemClickListener){
            itemView.setOnClickListener { listener.onItemClick(student) }

            itemView.setOnLongClickListener{listener.onLongItemClick(student)
                return@setOnLongClickListener true
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_adapter,parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return ListStudent.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemStudent = ListStudent[position]

        val sImage: ByteArray? = itemStudent.avatar
        val arrayInputStream = ByteArrayInputStream(sImage)
        val bitmap = BitmapFactory.decodeStream(arrayInputStream)


        holder.tv_masv.setText(itemStudent?.masv).toString()
        holder.tv_name.setText(itemStudent?.name).toString()
        holder.tv_birthday.setText(itemStudent?.birthday).toString()
        holder.tv_address.setText(itemStudent?.address).toString()
        holder.tv_gender.setText(itemStudent.gender).toString()
        holder.tv_specialized.setText(itemStudent?.specialized.toString())
        holder.img_avatar.setImageBitmap(byteArrayToBitmap(itemStudent.avatar))


        holder.bindView(ListStudent[position],listener)

        holder.bindView(ListStudent[position],listener)
    }

    fun resert(ListPerson: MutableList<Student>){
        this.ListStudent = ListPerson
        notifyDataSetChanged()
    }

    fun restoreItem(student: Student?, position: Int) {
        if (student != null) {
            ListStudent.add(position,student)
        }
        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        ListStudent.removeAt(position)
        notifyItemRemoved(position)
    }

    interface OnItemClickListener {
        fun onItemClick(student: Student )
        fun onLongItemClick(student: Student )
    }
    fun byteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
        val arrayInputStream = ByteArrayInputStream(byteArray)
        return BitmapFactory.decodeStream(arrayInputStream)
    }


}



