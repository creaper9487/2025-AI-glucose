package rl.collab.aiglucose.onClick

import android.view.View
import android.widget.EditText
import com.google.gson.Gson
import rl.collab.aiglucose.Client
import rl.collab.aiglucose.R
import rl.collab.aiglucose.Request
import rl.collab.aiglucose.customDialog
import rl.collab.aiglucose.frag.AccFrag
import rl.collab.aiglucose.setPosBntOnClick
import rl.collab.aiglucose.str
import java.io.File

class LogInBtnOnClick(private val accFrag: AccFrag) : View.OnClickListener {
    private val context = accFrag.requireContext()

    override fun onClick(p0: View?) {
        val view = View.inflate(context, R.layout.dialog_login, null)
        val pwIv = PwIvOnClick()
        pwIv.onClick(view)

        val accEt = view.findViewById<EditText>(R.id.acc_et)
        val pwEt = view.findViewById<EditText>(R.id.pw_et)

        val dialog = context.customDialog(R.string.log_in, view)

        val file = File(context.filesDir, AccFrag.ACC_JSON)
        if (file.exists()) {
            val req = Gson().fromJson(file.reader(), Request.LogIn::class.java)
            accEt.setText(req.username_or_email)

            if (accFrag.hasBio)
                accFrag.bioAuth({
                    Client.logIn(accFrag, req, dialog)
                }, {})
        }

        dialog.setPosBntOnClick {
            var valid = true
            val acc = accEt.str
            val pw = pwEt.str

            if (acc.isEmpty()) {
                accEt.setText(context.getString(R.string.invalid_acc))
                valid = false
            }
            if (pw.isEmpty()) {
                pwIv.show()
                pwEt.setText(context.getString(R.string.invalid_pw))
                valid = false
            }
            if (!valid) return@setPosBntOnClick

            Client.logIn(accFrag, Request.LogIn(acc, pw), dialog)
        }
    }
}