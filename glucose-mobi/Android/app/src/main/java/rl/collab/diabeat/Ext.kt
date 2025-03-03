package rl.collab.diabeat

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

val AlertDialog.neg
    get() = getButton(AlertDialog.BUTTON_NEGATIVE)!!

val AlertDialog.neutral
    get() = getButton(AlertDialog.BUTTON_NEUTRAL)!!

fun AlertDialog.setCancelJob(btn: Button = neg) =
    btn.setOnClickListener {
        dismiss()
        Client.job?.cancel()
    }

fun Context.dialog(
    title: String,
    msg: String? = null,
    view: View? = null,
    pos: String? = "OK",
    neg: String? = null,
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
    neg: String? = null,
    neutral: String? = null,
    cancelable: Boolean = false
) =
    requireContext().dialog(title, msg, view, pos, neg, neutral, cancelable)

fun Fragment.errDialog(msg: String, neutral: String? = null) =
    dialog("錯誤", msg, neutral = neutral)

fun Fragment.setHostErrDialog(msg: String) {
    val dialog = errDialog(msg, "設定 Host")
    dialog.neutral.setOnClickListener {
        dialog.dismiss()
        (requireActivity() as MainActivity).setHost()
    }
}

fun Fragment.exceptionDialog(e: Exception) {
    val z = e::class.java
    errDialog("${z.`package`?.name}\n${z.simpleName}\n\n${e.localizedMessage}")
}

fun Context.viewDialog(title: String, view: View) =
    dialog(title, view = view, neg = "取消")

fun Fragment.viewDialog(title: String, view: View, neutral: String? = null) =
    dialog(title, view = view, neg = "取消", neutral = neutral)

fun Fragment.io(block: suspend CoroutineScope.() -> Any?) =
    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) { block() }

suspend fun Fragment.ui(block: suspend Fragment.() -> Any?) =
    withContext(Dispatchers.Main) { block() }

inline fun SharedPreferences.syncEdit(action: SharedPreferences.Editor.() -> Unit) =
    edit(true, action)