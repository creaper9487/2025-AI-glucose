package rl.collab.diabeat

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun nacho(vararg msg: Any?) {
    Log.d("nacho", msg.joinToString(" "))
}

inline val Long.fmt
    get(): String {
        return if (this > 1048576) String.format("%.1f MB", this / 1048576.0)
        else String.format("%.1f KB", this / 1024.0)
    }

inline val String.isEmail get() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

inline val String.bearer get() = "Bearer $this"

inline val EditText.str get() = text.toString()

fun Context.customDialog(title: String, view: View, neutralBtn: String? = null): AlertDialog {
    val builder = AlertDialog.Builder(this)
        .setCancelable(false)
        .setTitle(title)
        .setView(view)
        .setPositiveButton("OK", null)
        .setNegativeButton("取消", null)

    neutralBtn?.let {
        builder.setNeutralButton(it, null)
    }

    return builder.show()
}

inline val AlertDialog.posBtn get() = getButton(AlertDialog.BUTTON_POSITIVE)!!

inline val AlertDialog.neutralBtn get() = getButton(AlertDialog.BUTTON_NEUTRAL)!!

fun Fragment.shortToast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.io(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.IO, block = block)
}

inline fun Fragment.ui(crossinline block: Fragment.() -> Unit) {
    requireActivity().runOnUiThread { block() }
}

fun Fragment.errDialog(msg: String) {
    AlertDialog.Builder(requireContext())
        .setCancelable(false)
        .setTitle("錯誤")
        .setMessage(msg)
        .setPositiveButton("OK", null)
        .show()
}