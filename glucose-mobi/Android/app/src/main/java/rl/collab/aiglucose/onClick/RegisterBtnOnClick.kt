package rl.collab.aiglucose.onClick

import android.view.View
import android.widget.EditText
import rl.collab.aiglucose.Client
import rl.collab.aiglucose.R
import rl.collab.aiglucose.Request
import rl.collab.aiglucose.customDialog
import rl.collab.aiglucose.frag.AccFrag
import rl.collab.aiglucose.isEmail
import rl.collab.aiglucose.setPosBntOnClick
import rl.collab.aiglucose.str

class RegisterBtnOnClick(private val accFrag: AccFrag) : View.OnClickListener {
    private val context = accFrag.requireContext()

    override fun onClick(p0: View?) {
        val view = View.inflate(context, R.layout.dialog_register, null)
        val pwIv = PwIvOnClick()
        pwIv.onClick(view)

        val emailEt = view.findViewById<EditText>(R.id.email_et)
        val usernameEt = view.findViewById<EditText>(R.id.username_et)
        val pwEt = view.findViewById<EditText>(R.id.pw_et)

        val dialog = context.customDialog(R.string.register, view)
        dialog.setPosBntOnClick {
            var valid = true
            val email = emailEt.str
            val username = usernameEt.str
            val pw = pwEt.str

            if (!email.isEmail) {
                emailEt.setText(context.getString(R.string.invalid_email))
                valid = false
            }
            if (username.isEmpty()) {
                usernameEt.setText(context.getString(R.string.invalid_username))
                valid = false
            }
            if (pw.isEmpty()) {
                pwIv.show()
                pwEt.setText(context.getString(R.string.invalid_pw))
                valid = false
            }
            if (!valid) return@setPosBntOnClick

            Client.register(accFrag, Request.Register(email, username, pw), dialog)
        }
    }
}