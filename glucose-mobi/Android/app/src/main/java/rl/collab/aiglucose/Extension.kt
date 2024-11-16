package rl.collab.aiglucose

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import java.util.Calendar

fun ll(vararg msg: Any) = Log.d("666", msg.joinToString(" "))
val date: String
    get() {
        Calendar.getInstance().apply {
            return "${get(Calendar.MONTH) + 1}/${get(Calendar.DATE)} ${"一二三四五六日"[get(Calendar.DAY_OF_WEEK) - 1]}"
        }
    }


val String.isEmail: Boolean get() = "^[^@]+@[^@]+$".toRegex().matches(this)
val String.isAlNum: Boolean get() = all { it.isLetterOrDigit() }


val EditText.str: String get() = text.toString()
fun AlertDialog.setPosBtnOnClick(l: View.OnClickListener) = getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(l)


fun Context.customDialog(titleStrRes: Int, view: View): AlertDialog =
    AlertDialog.Builder(this)
        .setCancelable(false)
        .setTitle(getString(titleStrRes))
        .setView(view)
        .setPositiveButton(getString(R.string.ok), null)
        .setNegativeButton(getString(R.string.cancel), null)
        .show()

fun Context.colorFromId(id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return typedValue.data
}

val Context.fgColor: Int get() = colorFromId(com.google.android.material.R.attr.colorOnSurface)
val Context.bgColor: Int get() = colorFromId(com.google.android.material.R.attr.colorSurface)