package com.vncoder.roomstudent

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vncoder.roomstudent.Adapter.StudentAdapter
import com.vncoder.roomstudent.Data.StudentDatabase
import com.vncoder.roomstudent.Entity.Student
import es.dmoral.toasty.Toasty
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.edit_student.*
import kotlinx.android.synthetic.main.edit_student.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(),CoroutineScope {
    private val ActivityRequestCode = 1
    private var studentDatabase : StudentDatabase? = null
    private var ListStudent  = mutableListOf<Student>()
    private var studentAdapter = StudentAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var recyclerView = findViewById<RecyclerView>(R.id.rv_recycleView)
        var sw_refresh = findViewById<SwipeRefreshLayout>(R.id.sw_refresh)
        var searchView = findViewById<SearchView>(R.id.searchView)

        studentDatabase = StudentDatabase.getData(this)




        resetList()

        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                launch {
                    var firstName = studentDatabase?.studentDao()?.findByFirstName(query.toString()) as MutableList<Student>
                    studentAdapter = StudentAdapter(listener,firstName)
                    recyclerView.adapter = studentAdapter

                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                launch {
                    ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                    studentAdapter = StudentAdapter(listener,ListStudent)
                    recyclerView.adapter = studentAdapter
                }
                studentAdapter.notifyDataSetChanged()
                return false
            }
        })
        launch {
                ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                studentAdapter = StudentAdapter(listener,ListStudent)
                recyclerView.adapter=studentAdapter
                studentAdapter.resert(ListStudent)
                studentAdapter.notifyDataSetChanged()
        }

        studentAdapter.resert(ListStudent)
        studentAdapter.notifyDataSetChanged()

        btn_addStudent.setOnClickListener {
            var intent =Intent(this,CreateStudent::class.java)
            startActivityForResult(intent,ActivityRequestCode)
        }

        btn_deleteAll.setOnClickListener {
            launch {
                studentDatabase?.studentDao()?.deleteAll()
                ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                studentAdapter.resert(ListStudent)
                studentAdapter.notifyDataSetChanged()
            }
            Toasty.error(this,"delete all sucess",Toast.LENGTH_LONG).show()
        }
        listener



        var itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder)
                    : Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                var position: Int= viewHolder.adapterPosition


                launch {
                    studentDatabase?.studentDao()?.delete(ListStudent[position])
                    ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                    studentAdapter = StudentAdapter(listener,ListStudent)
                    recyclerView.adapter = studentAdapter
                }
                studentAdapter.resert(ListStudent)
                studentAdapter.notifyDataSetChanged()

            Toasty.success(this@MainActivity,"delete Item Sucess",Toast.LENGTH_LONG).show()

            }

            override fun onChildDraw(c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
            RecyclerViewSwipeDecorator.Builder(this@MainActivity,c,recyclerView,viewHolder,dX,dX,actionState,isCurrentlyActive)
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.red008577))
                .addSwipeLeftActionIcon(R.drawable.ic_delete)
                .create()
                .decorate()
                super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive
                )
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityRequestCode && resultCode == Activity.RESULT_OK) {

            launch {
                var student : Student = data?.getSerializableExtra("extraPeople") as Student
                studentDatabase?.studentDao()?.insert(student)
                ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                studentAdapter.resert(ListStudent)
                studentAdapter.notifyDataSetChanged()
            }
            Toasty.success(this,"success",Toast.LENGTH_LONG).show()
           }else{
            Toasty.error(this,"not add student",Toast.LENGTH_LONG).show()
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var listener = object : StudentAdapter.OnItemClickListener{
        override fun onItemClick(student: Student) {

            val mDialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.edit_student, null)
            val mBuilder = AlertDialog.Builder(this@MainActivity).setView(mDialogView)
            val mAlertDialog = mBuilder.show()

            mAlertDialog.img_avatarEdit.setImageURI(Uri.parse(student.avatar))
            mDialogView.massvEdit.setText(student.masv).toString()
            mDialogView.edt_nameEdit.setText(student.name).toString()
            mDialogView.edt_addressEdit.setText(student.address ).toString()
            mDialogView.edt_birthdayEdit.setText(student.birthday ).toString()
            mDialogView.edt_specializedEdit.setText(student.specialized).toString()
            mDialogView.rd_genderEdit.checkedRadioButtonId

            mDialogView.btn_doneEdit.setOnClickListener {
                var massv:String = mDialogView.massvEdit.text.toString()
                var nameEit:String = mDialogView.edt_nameEdit.text.toString()
                var addressEit:String = mDialogView.edt_addressEdit.text.toString()
                var birthhdayEit:String = mDialogView.edt_birthdayEdit.text.toString()
                var skillEit:String = mDialogView.edt_specializedEdit.text.toString()

                var gender = mDialogView.rd_genderEdit.checkedRadioButtonId
                var isChecked = mDialogView.findViewById<RadioButton>(gender)
                var check:String = isChecked.text.toString()


                launch {
                    studentDatabase?.studentDao()?.update(
                        massv = massv,
                        name = nameEit,
                        birthday = birthhdayEit,
                        address = addressEit,
                        gender = check,
                        specialized = skillEit
                    )

                    ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                    studentAdapter.resert(ListStudent)
                    studentAdapter.notifyDataSetChanged()
                }
                Toasty.success(this@MainActivity,"update sucess",Toast.LENGTH_LONG).show()
                mAlertDialog.dismiss()
            }

            mDialogView.btn_cancelEdit.setOnClickListener {
                mAlertDialog.dismiss()
            }
            mDialogView.edt_birthdayEdit.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(this@MainActivity,
                    DatePickerDialog.OnDateSetListener { datePicker, mYear, mMonth, mDay ->
                        mDialogView.edt_birthdayEdit.setText(""+mDay+"/"+mMonth+"/"+mYear)
                    },year,month,day)
                datePickerDialog.show()
            }
        }
    }


    fun resetList(){
        sw_refresh.setOnRefreshListener {
            launch {
                ListStudent = studentDatabase?.studentDao()?.sortName() as MutableList<Student>
                studentAdapter.resert(ListStudent)
                studentAdapter.notifyDataSetChanged()
                sw_refresh.isRefreshing = false
            }

        }
    }



}