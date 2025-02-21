package rl.collab.diabeat.onclick

import android.text.Editable
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import rl.collab.diabeat.Client
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.DialogRegisterBinding
import rl.collab.diabeat.frag.AccFrag
import rl.collab.diabeat.isEmail
import rl.collab.diabeat.str
import rl.collab.diabeat.viewDialog

class RegisterBtnListener(private val accFrag: AccFrag) : View.OnClickListener {

    override fun onClick(p0: View?) {
        val binding = DialogRegisterBinding.inflate(accFrag.layoutInflater)

        binding.run {
            val dialog = accFrag.viewDialog("註冊", root, null)

            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            posBtn.isEnabled = false
            posBtn.setOnClickListener {
                val obj = Request.Register(emailEt.str, usernameEt.str, pwEt.str)
                Client.register(accFrag, obj, dialog::dismiss)
            }
            val watcher = { _: Editable? ->
                posBtn.isEnabled = emailEt.str.isEmail && usernameEt.str.isNotEmpty() && pwEt.str.isNotEmpty()
            }
            emailEt.doAfterTextChanged(watcher)
            usernameEt.doAfterTextChanged(watcher)
            pwEt.doAfterTextChanged(watcher)
        }
    }
}