package rl.collab.diabeat.onclick

import android.text.Editable
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import rl.collab.diabeat.Client
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.DialogLoginBinding
import rl.collab.diabeat.frag.AccFrag
import rl.collab.diabeat.str
import rl.collab.diabeat.viewDialog

class LoginBtnListener(private val accFrag: AccFrag) : View.OnClickListener {

    override fun onClick(p0: View?) {
        val binding = DialogLoginBinding.inflate(accFrag.layoutInflater)

        binding.run {
            if (accFrag.accFile.exists()) {
                accEt.setText(accFrag.accFile.readText())
                remeCb.isChecked = true
            }

            val dialog = accFrag.viewDialog("登入", root, "生物辨識")

            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            posBtn.isEnabled = false
            posBtn.setOnClickListener {
                val obj = Request.Login(accEt.str.trim(), pwEt.str)
                Client.logIn(accFrag, obj, dialog::dismiss, remeCb.isChecked)
            }
            val watcher = { _: Editable? ->
                posBtn.isEnabled = accEt.str.isNotEmpty() && pwEt.str.isNotEmpty()
            }
            accEt.doAfterTextChanged(watcher)
            pwEt.doAfterTextChanged(watcher)

            val neutralBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            if (accFrag.pwFile.exists()) {
                neutralBtn.setOnClickListener {
                    val obj = Request.Login(accFrag.accFile.readText(), accFrag.pwFile.readText())
                    accFrag.bioLogIn(obj, dialog::dismiss, remeCb.isChecked)
                }
                neutralBtn.callOnClick()
            } else
                neutralBtn.isEnabled = false
        }
    }
}