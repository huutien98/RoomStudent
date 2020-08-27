package com.vncoder.roomstudent

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vncoder.roomstudent.Data.StudentDatabase
import com.vncoder.roomstudent.Entity.Student
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_create_student.*
import kotlinx.android.synthetic.main.activity_create_student.view.*
import kotlinx.android.synthetic.main.item_adapter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

class CreateStudent : AppCompatActivity(),CoroutineScope {
    private var REQUEST_SELECT_IMAGE =1
    private var studentDatabase: StudentDatabase? = null
     var list:List<String>?=null
    private var uriImage :String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_student)

        studentDatabase = StudentDatabase.getData(this)

        btn_avatar.setOnClickListener {
            val intent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, REQUEST_SELECT_IMAGE)
        }

        edt_masv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 0) {
                    edt_masv.setError("not null")
                } else {
                    edt_masv.setError(null)
                }
            }
            override fun afterTextChanged(s: Editable) {

            }
        })

        edt_name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 0) {
                    edt_name.setError("name is not null")
                } else {
                    edt_name.setError(null)
            }
            }
            override fun afterTextChanged(s: Editable) {}
        })


        edt_address.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 0) {
                    edt_address.setError("address is not null")
                } else {
                    edt_address.setError(null)
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })


        edt_skill.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 0) {
                    edt_skill.setError("majous is not null")
                } else {
                    edt_skill.setError(null)
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        btn_done.setOnClickListener {



            var avatar :String =  uriImage.toString()
            var masv :String = edt_masv.text.toString().trim()
            var name:String = edt_name.text.toString().trim()

            var gender = rd_gender.checkedRadioButtonId
            var isChecked = findViewById<RadioButton>(gender)
            if (btn_male.isChecked == true){
                gender = 1
            }else{
                gender = 0
            }

            var birthday:String = edt_birthday.text.toString().trim()
            var address:String = edt_address.text.toString().trim()
            var special:String = edt_skill.text.toString().trim()

            launch {
                if (masv.isEmpty()){
                    edt_masv.setError("Not null")
                    edt_masv.requestFocus()
                }else if (name.isEmpty()){
                    edt_name.setError("Not null")
                    edt_name.requestFocus()
                }else if (birthday.isEmpty()){
                    edt_birthday.setError("Not null")
                    edt_birthday.requestFocus()
                }else if (address.isEmpty()){
                    edt_address.setError("Not null")
                    edt_address.requestFocus()
                }else if (special.isEmpty()){
                    edt_skill.setError("Not null")
                    edt_skill.requestFocus()
                }else  {
                    list = studentDatabase?.studentDao()?.queryCode()
                    if (list!=null){
                        for (index in list!!.indices){
                            if ((masv.trim()).compareTo(list!![index],true) == 0){
                                val builder = AlertDialog.Builder(this@CreateStudent)
                                builder.setTitle("Error")
                                builder.setMessage("mã sinh viên bạn nhập đã có")
                                builder.setPositiveButton("Nhập lại"){ dialog, which ->
                                    dialog.dismiss()
                                }
                                val dialog: AlertDialog = builder.create()
                                dialog.show()
                               return@launch
                            }
                        }
                    }
                        val replyIntent = Intent()
                        var student = Student(id = null,
                            avatar = avatar,
                            masv = masv,
                            name = name,
                            birthday = birthday,
                            gender = gender,
                            address = address,
                            specialized = special)
                        replyIntent.putExtra("extraPeople",student)
                        setResult(Activity.RESULT_OK, replyIntent)
                        finish()
                }
            }

        }

        btn_cancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        edt_birthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { datePicker, mYear, mMonth, mDay ->
                    edt_birthday.setText("" + mDay + "/" + mMonth + "/" + mYear)
                }, year, month, day)
            datePickerDialog.show()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGE && data != null && data.data != null) {
            uriImage =data.data.toString()
            btn_avatar.setImageURI(Uri.parse(uriImage))
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main





}