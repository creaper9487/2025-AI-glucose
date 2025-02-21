package rl.collab.diabeat

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun nacho(vararg msg: Any?) {
    Log.d("nacho", msg.joinToString(" "))
}

fun Long.fmt(): String {
    return if (this > 1048576)
        "%.1f MB".format(this / 1048576.0)
    else
        "%.1f KB".format(this / 1024.0)
}

fun Double?.tryToInt(): String {
    return if (this == null)
        "-"
    else if (this % 1.0 == 0.0)
        toInt().toString()
    else
        toString()
}

val String.isEmail get() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.localDT(): String {
    val localDateTime = Instant.parse(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val now = LocalDate.now()

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

val EditText.str get() = text.toString()

fun Fragment.toast(msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.dialog(title: String, msg: String) {
    MaterialAlertDialogBuilder(requireContext())
        .setCancelable(false)
        .setTitle(title)
        .setMessage(msg)
        .setPositiveButton("OK", null)
        .show()
}

fun Fragment.errDialog(msg: String) {
    dialog("錯誤", msg)
}

fun Context.viewDialog(title: String, view: View?, neutralBtn: String?): AlertDialog {
    val builder = MaterialAlertDialogBuilder(this)
        .setCancelable(false)
        .setTitle(title)
        .setPositiveButton("OK", null)
        .setNegativeButton("取消", null)

    view?.let {
        builder.setView(it)
    }

    neutralBtn?.let {
        builder.setNeutralButton(it, null)
    }

    return builder.show()
}

fun Fragment.viewDialog(title: String, view: View?, neutralBtn: String?): AlertDialog =
    requireContext().viewDialog(title, view, neutralBtn)

fun LifecycleOwner.io(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.IO, block = block)
}

suspend inline fun Fragment.ui(crossinline block: Fragment.() -> Unit) {
    withContext(Dispatchers.Main) { block() }
}