package rl.collab.aiglucose

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

fun nacho(vararg msg: Any) {
    Log.d("nacho", msg.joinToString(" "))
}

val String.isEmail: Boolean get() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

val EditText.str: String get() = text.toString()

fun Context.customDialog(titleStrRes: Int, view: View) =
    AlertDialog.Builder(this)
        .setCancelable(false)
        .setTitle(getString(titleStrRes))
        .setView(view)
        .setPositiveButton(getString(R.string.ok), null)
        .setNegativeButton(getString(R.string.cancel), null)
        .show()

fun AlertDialog.setPosBntOnClick(listener: View.OnClickListener) {
    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(listener)
}

fun Fragment.io(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.IO, block = block)
}

fun Fragment.ui(block: Fragment.() -> Unit) {
    requireActivity().runOnUiThread { block() }
}

fun Fragment.errDialog(msg: String) {
    AlertDialog.Builder(this.requireContext())
        .setCancelable(false)
        .setTitle(getString(R.string.err))
        .setMessage(msg)
        .setPositiveButton(getString(R.string.ok), null)
        .show()
}