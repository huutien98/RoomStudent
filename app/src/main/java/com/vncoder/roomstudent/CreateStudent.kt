package com.vncoder.roomstudent

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vncoder.roomstudent.Data.StudentDatabase
import com.vncoder.roomstudent.Entity.Student
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_create_student.*
import kotlinx.android.synthetic.main.item_adapter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class CreateStudent : AppCompatActivity(),CoroutineScope {
    private var REQUEST_SELECT_IMAGE =1
    private var studentDatabase: StudentDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_student)

        studentDatabase = StudentDatabase.getData(this)


        btn_avatar.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                     val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                     requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                     pickImageFromGallery();
                }
            }
            else{
                 pickImageFromGallery();
            }

        }

        edt_masv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 0) {
                    edt_masv.setError("id is not null")
                } else {
                    edt_masv.setError(null)
                }
            }
            override fun afterTextChanged(s: Editable) {
                if (s.toString() == ""){
                    val builder = AlertDialog.Builder(this@CreateStudent)
                    builder.setTitle("Error")
                    builder.setMessage("masv is not null")
                    builder.setPositiveButton("OKE"){ dialog, which ->
                        //Toast.makeText(applicationContext,"continuar",Toast.LENGTH_SHORT).show()
                    }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }

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
                    edt_address.setError("name is not null")
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
                    edt_skill.setError("name is not null")
                } else {
                    edt_skill.setError(null)
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })


        btn_done.setOnClickListener {
            var avatar :String = btn_avatar.toString()
            var masv :String = edt_masv.text.toString().trim()
            var name:String = edt_name.text.toString().trim()
            var gender = rd_gender.checkedRadioButtonId
            var isChecked = findViewById<RadioButton>(gender)
            var birthday:String = edt_birthday.text.toString().trim()
            var check:String = isChecked.text.toString().trim()
            var address:String = edt_address.text.toString().trim()
            var special:String = edt_skill.text.toString().trim()

            val replyIntent = Intent()

                var student = Student(id = null,avatar = avatar,masv = masv,name = name,birthday = birthday,gender = check,address = address,specialized = special)
                replyIntent.putExtra("extraPeople",student)
                setResult(Activity.RESULT_OK, replyIntent)
           
            finish()
            Toasty.success(this,"create Item Sucess",Toast.LENGTH_LONG).show()

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

    private fun pickImageFromGallery() {
         val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    companion object {
         private val IMAGE_PICK_CODE = 1000;
         private val PERMISSION_CODE = 1001;
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                     pickImageFromGallery()
                }
                else{
                     Toasty.success(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            btn_avatar.setImageURI(data?.data)
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}