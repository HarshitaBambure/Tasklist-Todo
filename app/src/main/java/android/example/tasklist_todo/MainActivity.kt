package android.example.tasklist_todo

//import com.example.awsomeuisamples.helpers.getAndroidId
//import com.example.awsomeuisamples.helpers.showDialog


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color.red
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.sip.SipAudioCall
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*


//Get Access to Firebase database, no need of any URL, Firebase
//identifies the connection via the package name of the app

class MainActivity : AppCompatActivity() {

    private lateinit var alertDialog: AlertDialog
    private lateinit var adapter: ToDoItemAdapter
    lateinit var mDatabase: DatabaseReference
    var toDoItemList = mutableListOf<ToDoItem>()
    var isNotfirstTime = false
    val KEY_SHAREPREF = "SHAREPREF_KEY";
    val KEY_ISFIRSTTIME = "is_firstTime";
    lateinit var mSharePref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSharePref = getSharedPreferences(KEY_SHAREPREF, Context.MODE_PRIVATE)
        isNotfirstTime = mSharePref.getBoolean(
            KEY_ISFIRSTTIME,
            false
        )
        addFirebaseListener()
        //Adding click listener for FAB
        fab.setOnClickListener { view ->
            //Show Dialog here to add new Item
            addNewItemDialog()
        }
        if (!isNotfirstTime) {
            showInfoDialog(R.id.fab, true, getString(R.string.click_here), getString(R.string.msg_add_note))
        }
    }

    fun showInfoDialog(id: Int, isForAdd: Boolean, title: String, msg: String) {
        val target = TapTarget.forView(
            findViewById(id),
            title,
            msg
        ) // All options below are optional
            .outerCircleColor(R.color.red) // Specify a color for the outer circle
            .outerCircleAlpha(0.96f) // Specify the alpha amount for the outer circle
            .targetCircleColor(R.color.white) // Specify a color for the target circle
            .titleTextSize(20) // Specify the size (in sp) of the title text
            .titleTextColor(R.color.white) // Specify the color of the title text
            .descriptionTextSize(10) // Specify the size (in sp) of the description text
            .descriptionTextColor(R.color.red) // Specify the color of the description text
            .textColor(R.color.blue) // Specify a color for both the title and description text
            .textTypeface(Typeface.SANS_SERIF) // Specify a typeface for the text
            .dimColor(R.color.black) // If set, will dim behind the view with 30% opacity of the given color
            .drawShadow(true) // Whether to draw a drop shadow or not
            .cancelable(false) // Whether tapping outside the outer circle dismisses the view
            .tintTarget(true) // Whether to tint the target view's color
            .transparentTarget(false) // Specify whether the target is transparent (displays the content underneath)
            // Specify a custom drawable to draw as the target
            .targetRadius(60)  // Specify the target radius (in dp)
        object : SipAudioCall.Listener() {
            // The listener can listen for regular clicks, long clicks or cancels
            fun onTargetClick(view: TapTargetView?) {
            }
        }

        TapTargetView.showFor(this, target
            , object : TapTargetView.Listener() {

            })

    }

    fun addNewItemDialog() {
        val alert = AlertDialog.Builder(this)

        val itemEditText = EditText(this)
        alert.setMessage(getString(R.string.msg_add_item))
        alert.setTitle(getString(R.string.msg_enter_item_text))
        alert.setCancelable(false)

        alert.setView(itemEditText)

        alert.setPositiveButton(getString(R.string.msg_submit)) { dialog, positiveButton ->

            if (!itemEditText?.text.isNotEmpty()) {
                showToast(this, getString(R.string.msg_note_cant_empty))
            } else {
                val todoItem = ToDoItem.create()
                todoItem.itemText = itemEditText.text.toString()
                todoItem.done = false

                //We first make a push so that a new item is made with a unique ID
                val newItem = mDatabase.child(getAndroidId()).push()
                todoItem.objectId = newItem.key

                //then, we used the reference to set the value on that ID
                newItem.setValue(todoItem)

                dialog.dismiss()
                showToast(this, getString(R.string.msg_save))
                if (!isNotfirstTime) {
                    isNotfirstTime = true

                    showInfoDialog(
                        R.id.listViewItems,
                        false,
                        getString(R.string.title_long_press),
                        getString(R.string.msg_delete_update)
                    )
                }

            }
        }

        alert.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSharePref.edit()
            .putBoolean(KEY_ISFIRSTTIME, true).commit()

    }

    fun addFirebaseListener() {

        var itemListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                addDataToList(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Item failed, log a message
                Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
            }

            private fun addDataToList(dataSnapshot: DataSnapshot) {

                toDoItemList.clear()
                val items = dataSnapshot.child(getAndroidId()).children.iterator()
                //Check if current database contains any collection
                while (items.hasNext()) {


                    //get current item
                    val currentItem = items.next()
                    val todoItem = ToDoItem.create()


                    //get current data in a map
                    val map = currentItem.getValue() as HashMap<String, Any>


                    //key will return Firebase ID
                    todoItem.objectId = currentItem.key
                    todoItem.done = map.get("done") as Boolean?
                    todoItem.itemText = map.get("itemText") as String?

                    toDoItemList!!.add(todoItem);
                }


                adapter = ToDoItemAdapter(this@MainActivity, toDoItemList!!)

                listViewItems!!.setAdapter(adapter)
                //alert adapter that has changed
                adapter.notifyDataSetChanged()
            }
            //   addNewItemDialog()
        }

        mDatabase = FirebaseDatabase.getInstance().reference

        mDatabase.orderByKey().addValueEventListener(itemListener)
        listViewItems.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { parent, view, position, id ->
                alertDialog =
                    showDialog(
                        getString(R.string.title_dialog),
                        getString(R.string.msg_update_Status),
                        getString(R.string.txt_delete),
                        getString(R.string.txt_change_task_status)
                    ) { isDelete ->
                        if (isDelete) {
                            modifyItemState(
                                toDoItemList.get(position).objectId!!,
                                !toDoItemList.get(position).done!!
                            )
                            alertDialog.dismiss()
                        } else {
                            deleteItem(toDoItemList.get(position).objectId!!)
                            alertDialog.dismiss()
                        }
                    }
                true
            }

    }

    fun deleteItem(id: String) {
        onItemDelete(id)
    }

    fun modifyItemState(itemObjectId: String, isDone: Boolean) {
        val itemReference = mDatabase.child(getAndroidId()).child(itemObjectId)
        itemReference.child("done").setValue(isDone);
    }


    //delete an item
    fun onItemDelete(itemObjectId: String) {
        //get child reference in database via the ObjectID
        val itemReference = mDatabase.child(getAndroidId()).child(itemObjectId)
        //deletion can be done via removeValue() method
        itemReference.removeValue()
    }

}