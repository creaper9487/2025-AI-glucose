package rl.collab.diabeat.click

import android.view.View
import android.widget.EditText
import rl.collab.diabeat.Client
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.customDialog
import rl.collab.diabeat.frag.AccFrag
import rl.collab.diabeat.posBtn
import rl.collab.diabeat.str

class RegisterBtnClick(private val accFrag: AccFrag) : View.OnClickListener {
    private val context = accFrag.requireContext()

    override fun onClick(v0: View) {
        val view = View.inflate(context, R.layout.dialog_register, null)

        val emailEt = view.findViewById<EditText>(R.id.email_et)
        val usernameEt = view.findViewById<EditText>(R.id.username_et)
        val pwEt = view.findViewById<EditText>(R.id.pw_et)

        val dialog = context.customDialog("註冊", view)
        dialog.posBtn.isEnabled = false
        dialog.posBtn.setOnClickListener {
            val obj = Request.Register(emailEt.str, usernameEt.str, pwEt.str)
            Client.register(accFrag, obj, dialog::dismiss)
        }
        AccPosBtnWatcher(view, false, dialog.posBtn)
    }
}