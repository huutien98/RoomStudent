package com.vncoder.roomstudent

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.collection.LruCache
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vncoder.roomstudent.Adapter.StudentAdapter
import com.vncoder.roomstudent.Data.StudentDatabase
import com.vncoder.roomstudent.Entity.Student
import es.dmoral.toasty.Toasty
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.edit_student.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(),CoroutineScope {
    private var REQUEST_SELECT_IMAGE =200
    private val ActivityRequestCode = 1
    private var studentDatabase : StudentDatabase? = null
    private var ListStudent  = mutableListOf<Student>()
    private var studentAdapter = StudentAdapter()
    private var bitmap: Bitmap? = null
    private var mDialogView: View? = null
    private var imageUri :Uri? = null
    private var mMemoryCache: LruCache<String, Bitmap>? = null

    private val actionMode: ActionMode? = null
    private val toolbar: Toolbar? = null
    private var recyclerView:RecyclerView?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView  = findViewById<RecyclerView>(R.id.rv_recycleView)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        studentDatabase = StudentDatabase.getData(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        ClickButton()
        resetList()

        launch {
                ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                recyclerView?.layoutManager = LinearLayoutManager(this@MainActivity)
                studentAdapter = StudentAdapter(listener,ListStudent)
                recyclerView?.adapter=studentAdapter
                studentAdapter.resert(ListStudent)
                studentAdapter.notifyDataSetChanged()
                recyclerView?.itemAnimator = SlideInLeftAnimator()
        }

        studentAdapter.resert(ListStudent)
        studentAdapter.notifyDataSetChanged()

        deleteAll()

        btn_addStudent.setOnClickListener {
            var intent =Intent(this,CreateStudent::class.java)
            startActivityForResult(intent,ActivityRequestCode)
        }

        listener

        var itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder)
                    : Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                var position: Int= viewHolder.adapterPosition

                val deleteditem: Student = ListStudent.get(viewHolder.adapterPosition)
                studentAdapter.removeItem(position)
                studentAdapter.resert(ListStudent)
                studentAdapter.notifyDataSetChanged()
            Toasty.success(this@MainActivity,"delete Item Sucess",Toast.LENGTH_LONG).show()
                Snackbar.make(viewHolder.itemView,"do you want undo",Snackbar.LENGTH_LONG)
                    .setAction("Undo", View.OnClickListener {
                        studentAdapter.restoreItem(deleteditem,position)
                        }).show()

//                launch {
//                    studentDatabase?.studentDao()?.delete(ListStudent[position])
//                    ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
//                    studentAdapter = StudentAdapter(listener,ListStudent)
//                    recyclerView?.adapter = studentAdapter
//                }
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

        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            try {
                imageUri = data?.data
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                mDialogView?.img_avatarEdit?.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (requestCode == ActivityRequestCode && resultCode == Activity.RESULT_OK) {
            launch {
                var student : Student = data?.getSerializableExtra("extraPeople") as Student
                studentDatabase?.studentDao()?.insert(student)
                ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                studentAdapter.resert(ListStudent)
                studentAdapter.notifyDataSetChanged()
            }
           }

    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var listener = object : StudentAdapter.OnItemClickListener{
        override fun onItemClick(student: Student) {

            mDialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.edit_student, null)
            val mBuilder = AlertDialog.Builder(this@MainActivity).setView(mDialogView)
            val mAlertDialog = mBuilder.show()

            val sImage: ByteArray? = student.avatar
            val arrayInputStream = ByteArrayInputStream(sImage)
            var bitmap = BitmapFactory.decodeStream(arrayInputStream)


            mDialogView?.img_avatarEdit?.setImageBitmap(bitmap)
            mDialogView?.massvEdit?.setText(student.masv).toString()
            mDialogView?.edt_nameEdit?.setText(student.name).toString()
            mDialogView?.edt_addressEdit?.setText(student.address ).toString()
            mDialogView?.edt_birthdayEdit?.setText(student.birthday ).toString()
            mDialogView?.edt_specializedEdit?.setText(student.specialized).toString()
            mDialogView?.rd_genderEdit?.checkedRadioButtonId



            mDialogView?.img_avatarEdit?.setOnClickListener{
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhoto, 200)

            }

            mDialogView?.img_avatarEdit?.setImageBitmap(byteArrayToBitmap(student.avatar))


            mDialogView?.btn_doneEdit?.setOnClickListener {
                val stream = ByteArrayOutputStream()
                this@MainActivity.bitmap?.compress(Bitmap.CompressFormat.PNG, 90, stream)
                val byteArray = stream.toByteArray()
                this@MainActivity.bitmap?.recycle()

                var avatar : ByteArray = byteArray
                var massv:String = mDialogView?.massvEdit?.text.toString()
                var nameEit:String = mDialogView?.edt_nameEdit?.text.toString()
                var addressEit:String = mDialogView?.edt_addressEdit?.text.toString()
                var birthhdayEit:String = mDialogView?.edt_birthdayEdit?.text.toString()
                var skillEit:String = mDialogView?.edt_specializedEdit?.text.toString()

                var gender = mDialogView?.rd_genderEdit?.checkedRadioButtonId
                var isChecked = gender?.let { it1 -> mDialogView?.findViewById<RadioButton>(it1) }
                var check:String = isChecked?.text.toString()


                launch {
                    studentDatabase?.studentDao()?.update(
                        avatar = avatar,
                        masv = massv,
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

            mDialogView?.btn_cancelEdit?.setOnClickListener {
                mAlertDialog.dismiss()
            }
            mDialogView?.edt_birthdayEdit?.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(this@MainActivity,
                    DatePickerDialog.OnDateSetListener { datePicker, mYear, mMonth, mDay ->
                        mDialogView?.edt_birthdayEdit?.setText(""+mDay+"/"+mMonth+"/"+mYear)
                    },year,month,day)
                datePickerDialog.show()
            }
        }

        override fun onLongItemClick(student: Student) {

            Toast.makeText(this@MainActivity,"this is long click",Toast.LENGTH_LONG).show()
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

    fun ClickButton(){
        var isOpen = false
        val fabOpen = AnimationUtils.loadAnimation(this,R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(this,R.anim.fab_close)
        val fabRclockwise = AnimationUtils.loadAnimation(this,R.anim.rolate)
        val fabAntiClockwise = AnimationUtils.loadAnimation(this,R.anim.rolate2)

        btn_select.setOnClickListener {
            if (isOpen){
                btn_addStudent.startAnimation(fabClose)
                btn_deleteAll.startAnimation(fabClose)
                btn_select.startAnimation(fabRclockwise)
                isOpen = false
            }else{
                btn_addStudent.startAnimation(fabOpen)
                btn_deleteAll.startAnimation(fabOpen)
                btn_select.startAnimation(fabAntiClockwise)

                btn_addStudent.isClickable
                btn_deleteAll.isClickable

                isOpen = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu , menu)

        var menuItem : MenuItem? = menu?.findItem(R.id.action_search)
        var searchView : SearchView = menuItem?.actionView as SearchView
        searchView.queryHint = "input text"
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.toString().isEmpty()){
                    launch {
                        var firstName = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                        studentAdapter = StudentAdapter(listener,firstName)
                        recyclerView?.adapter = studentAdapter
                    }
                    studentAdapter.notifyDataSetChanged()
                }else{
                    launch {
                        ListStudent = studentDatabase?.studentDao()?.findByFirstName(newText.toString()) as MutableList<Student>
                        studentAdapter = StudentAdapter(listener,ListStudent)
                        recyclerView?.adapter = studentAdapter
                        recyclerView?.itemAnimator = SlideInLeftAnimator()
                    }
                    studentAdapter.notifyDataSetChanged()
                }
                return false
            }

        })

        return true
    }

    fun deleteAll(){
        btn_deleteAll.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Monstarlab lifeTime")
            builder.setMessage("Do you want delete all item ?")
            builder.setCancelable(false)
            builder.setPositiveButton("Yes")
            { dialogInterface, i
                ->   launch {
                studentDatabase?.studentDao()?.deleteAll()
                ListStudent = studentDatabase?.studentDao()?.getAllPerson() as MutableList<Student>
                studentAdapter.resert(ListStudent)
                studentAdapter.notifyDataSetChanged()
                recyclerView?.itemAnimator = SlideInLeftAnimator()
            }
                Toasty.error(this,"delete all sucess",Toast.LENGTH_LONG).show() }
            builder.setNegativeButton("No")
            { dialogInterface, i -> dialogInterface.dismiss()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    fun byteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
        val arrayInputStream = ByteArrayInputStream(byteArray)
        return BitmapFactory.decodeStream(arrayInputStream)
    }
}