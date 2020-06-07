package android.example.tasklist_todo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


fun Activity.showLongToast(msg: String, isLong: Boolean) {
    Toast.makeText(this, msg, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun Toast.showToast(context: Context, msg: String, isLong: Boolean) {
    Toast.makeText(context, msg, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
}

@SuppressLint("HardwareIds")
fun Activity.getAndroidId(): String {
    return Settings.Secure.getString(
        getContentResolver(),
        Settings.Secure.ANDROID_ID
    );
}

fun String.shareTextUrl(activity: Activity, title: String) {
    val share = Intent(Intent.ACTION_SEND)
    share.type = "text/plain"
    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    // Add data to the intent, the receiving app will decide
    // what to do with it.
    share.putExtra(Intent.EXTRA_SUBJECT, title)
    share.putExtra(Intent.EXTRA_TEXT, this)
    activity.startActivity(Intent.createChooser(share, "Share link!"))
}

class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        outRect.left = space
        outRect.right = space
        outRect.bottom = space

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = space
        } else {
            outRect.top = 0
        }
        super.getItemOffsets(outRect, view, parent, state)
    }

}

fun Activity.showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}

fun Activity.showDialog(
    msg: String,
    title: String,
    btnok: String,
    btnCancel: String,
    btnClick: (isCancel: Boolean) -> Unit
): AlertDialog {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(msg)
    //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

    builder.setPositiveButton(btnok) { dialog, which ->
        btnClick(false)
    }

    builder.setNegativeButton(btnCancel) { dialog, which ->
        btnClick(true)
    }
    val dialog = builder.create()
    dialog.show()
    return dialog
}
