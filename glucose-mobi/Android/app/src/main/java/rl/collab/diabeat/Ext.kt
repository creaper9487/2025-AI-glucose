package rl.collab.diabeat

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun nacho(vararg msg: Any?) =
    Log.d("nacho", msg.joinToString(" "))

fun Long.fmt() =
    if (this > 1048576)
        "%.1f MB".format(this / 1048576.0)
    else
        "%.1f KB".format(this / 1024.0)

fun Double?.tryToInt() =
    if (this == null)
        "-"
    else if (this % 1.0 == 0.0)
        toInt().toString()
    else
        toString()

val String.isEmail
    get() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.localDateTime(): String {
    val localDateTime = Instant.parse(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val now = LocalDateTime.now()

    return localDateTime.format(
        DateTimeFormatter.ofPattern(
            if (localDateTime.year == now.year)
                if (localDateTime.dayOfYear == now.dayOfYear)
                    "HH:mm"
                else
                    "MM/dd HH:mm"
            else
                "yyyy/MM/dd HH:mm"
        )
    )
}

val EditText.str
    get() = text.toString()

fun EditText.hideKb() =
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(windowToken, 0)

fun Context.toast(msg: String) =
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Fragment.toast(msg: String) =
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

fun AlertDialog.pos() =
    getButton(AlertDialog.BUTTON_POSITIVE)!!

fun AlertDialog.neutral() =
    getButton(AlertDialog.BUTTON_NEUTRAL)!!

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

fun Fragment.excDialog(e: Exception) {
    val z = e::class.java
    dialog("錯誤", "${z.`package`?.name}\n${z.simpleName}\n\n${e.localizedMessage}")
}

fun Context.viewDialog(title: String, view: View) =
    dialog(title, view = view, neg = "取消")

fun Fragment.viewDialog(title: String, view: View, neutral: String? = null) =
    dialog(title, view = view, neg = "取消", neutral = neutral)

fun Fragment.io(block: suspend CoroutineScope.() -> Any?) =
    lifecycleScope.launch(Dispatchers.IO) { block() }

suspend inline fun Fragment.ui(crossinline block: Fragment.() -> Any?) =
    withContext(Dispatchers.Main) { block() }