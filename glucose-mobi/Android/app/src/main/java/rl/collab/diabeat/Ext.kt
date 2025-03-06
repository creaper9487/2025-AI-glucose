package rl.collab.diabeat

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun nacho(vararg msg: Any?) =
    Log.d("nacho", msg.joinToString(" "))

val EditText.str
    get() = text.toString()

fun Context.toast(msg: String) =
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Fragment.toast(msg: String) =
    requireContext().toast(msg)

val AlertDialog.pos
    get() = getButton(AlertDialog.BUTTON_POSITIVE)!!

val AlertDialog.ntr
    get() = getButton(AlertDialog.BUTTON_NEUTRAL)!!

fun Context.dialog(
    title: String,
    msg: String? = null,
    view: View? = null,
    pos: String? = "OK",
    neg: String? = "取消",
    neutral: String? = null,
    cancelable: Boolean = false
) =
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(msg)
        .setView(view)
        .setPositiveButton(pos, null)
        .setNegativeButton(neg, null)
        .setNeutralButton(neutral, null)
        .setCancelable(cancelable)
        .show()!!

fun Fragment.dialog(
    title: String,
    msg: String? = null,
    view: View? = null,
    pos: String? = "OK",
    neg: String? = "取消",
    neutral: String? = null,
    cancelable: Boolean = false
) =
    requireContext().dialog(title, msg, view, pos, neg, neutral, cancelable)

fun Fragment.errDialog(msg: String, neutral: String? = null) =
    dialog("錯誤", msg, neg = null, neutral = neutral)

fun Fragment.exceptionDialog(e: Exception) {
    val z = e::class.java
    errDialog("${z.`package`?.name}\n${z.simpleName}\n\n${e.localizedMessage}")
}

val Fragment.viewLifecycleScope get() = viewLifecycleOwner.lifecycleScope

inline fun SharedPreferences.syncEdit(action: SharedPreferences.Editor.() -> Unit) =
    edit(true, action)