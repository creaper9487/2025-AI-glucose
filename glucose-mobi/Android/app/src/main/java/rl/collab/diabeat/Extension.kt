package rl.collab.diabeat

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun nacho(vararg msg: Any?) {
    Log.d("nacho", msg.joinToString(" "))
}

inline val String.isEmail get() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

inline val EditText.str get() = text.toString()

fun Context.customDialog(titleId: Int, view: View, neutralBtnId: Int? = null): AlertDialog {
    val builder = AlertDialog.Builder(this)
        .setCancelable(false)
        .setTitle(titleId)
        .setView(view)
        .setPositiveButton(R.string.ok, null)
        .setNegativeButton(R.string.cancel, null)

    neutralBtnId?.let { builder.setNeutralButton(neutralBtnId, null) }
    return builder.show()
}

inline val AlertDialog.posBtn get() = getButton(AlertDialog.BUTTON_POSITIVE)!!

inline val AlertDialog.neutralBtn get() = getButton(AlertDialog.BUTTON_NEUTRAL)!!

fun Fragment.io(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.IO, block = block)
}

inline fun Fragment.ui(crossinline block: Fragment.() -> Unit) {
    requireActivity().runOnUiThread { block() }
}

fun Fragment.errDialog(msg: String) {
    AlertDialog.Builder(requireContext())
        .setCancelable(false)
        .setTitle(R.string.err)
        .setMessage(msg)
        .setPositiveButton(R.string.ok, null)
        .show()
}

fun Fragment.errDialog(msgId: Int) {
    errDialog(getString(msgId))
}