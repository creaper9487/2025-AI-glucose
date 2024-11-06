package rl.collab.aiglucose

import android.content.Context
import android.util.Log
import android.widget.Toast

fun ll(vararg msg: Any) = Log.d("666", msg.joinToString(" "))
fun tt(context: Context, vararg msg: Any) =
    Toast.makeText(context, msg.joinToString(" "), Toast.LENGTH_SHORT).show()