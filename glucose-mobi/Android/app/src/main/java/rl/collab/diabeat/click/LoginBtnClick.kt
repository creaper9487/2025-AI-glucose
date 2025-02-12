package rl.collab.diabeat.click

import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import rl.collab.diabeat.Client
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.customDialog
import rl.collab.diabeat.frag.AccFrag
import rl.collab.diabeat.neutralBtn
import rl.collab.diabeat.posBtn
import rl.collab.diabeat.str

class LoginBtnClick(private val accFrag: AccFrag) : View.OnClickListener {
    private val context = accFrag.requireContext()

    override fun onClick(v0: View) {
        val view = View.inflate(context, R.layout.dialog_login, null)

        val accEt = view.findViewById<EditText>(R.id.acc_et)
        val pwEt = view.findViewById<EditText>(R.id.pw_et)
        val remeCb = view.findViewById<CheckBox>(R.id.reme_cb)
        if (accFrag.accFile.exists()) {
            accEt.setText(accFrag.accFile.readText())
            remeCb.isChecked = true
        }

        val dialog = context.customDialog("登入", view, "生物辨識")
        dialog.posBtn.isEnabled = false
        dialog.posBtn.setOnClickListener {
            val obj = Request.Login(accEt.str.trim(), pwEt.str)
            Client.logIn(accFrag, obj, dialog::dismiss, remeCb.isChecked)
        }
        AccPosBtnWatcher(view, true, dialog.posBtn)

        if (accFrag.pwFile.exists()) {
            dialog.neutralBtn.setOnClickListener {
                val obj = Request.Login(accFrag.accFile.readText(), accFrag.pwFile.readText())
                accFrag.bioLogIn(obj, dialog::dismiss, remeCb.isChecked)
            }
            dialog.neutralBtn.callOnClick()
        } else
            dialog.neutralBtn.isEnabled = false
    }
}