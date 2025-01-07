package rl.collab.diabeat.onClick

import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import rl.collab.diabeat.R
import rl.collab.diabeat.isEmail
import rl.collab.diabeat.isUsername
import rl.collab.diabeat.str

class AccDialogPosBtnWatcher(view: View, isLogIn: Boolean, dialogPosBtn: Button) {
    private val pwIv = view.findViewById<ImageView>(R.id.show_hide_pw_iv)
    private val pwEt = view.findViewById<EditText>(R.id.pw_et)

    init {
        pwIv.setOnClickListener {
            if (pwEt.transformationMethod == null) // visible
                hidePw()
            else
                showPw()
        }

        val etGroup: Array<EditText> = if (isLogIn)
            arrayOf(view.findViewById(R.id.acc_et), pwEt)
        else
            arrayOf(view.findViewById(R.id.email_et), view.findViewById(R.id.username_et), pwEt)

        dialogPosBtn.isEnabled = false
        for (et in etGroup) {
            et.addTextChangedListener { _ ->
                dialogPosBtn.isEnabled = etGroup.all { it.str.isNotEmpty() } &&
                        if (isLogIn) {
                            true
                        } else {
                            etGroup[0].str.isEmail && etGroup[1].str.isUsername
                        }
            }
        }
    }

    private fun hidePw() {
        pwEt.transformationMethod = PasswordTransformationMethod.getInstance()
        pwIv.setImageResource(R.drawable.invisible)
    }

    private fun showPw() {
        pwEt.transformationMethod = null
        pwIv.setImageResource(R.drawable.visible)
    }
}