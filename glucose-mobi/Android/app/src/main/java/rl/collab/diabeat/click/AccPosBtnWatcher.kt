package rl.collab.diabeat.click

import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import rl.collab.diabeat.R
import rl.collab.diabeat.isEmail
import rl.collab.diabeat.str

class AccPosBtnWatcher(view: View, isLoggedIn: Boolean, dialogPosBtn: Button) {
    private val pwIv = view.findViewById<ImageView>(R.id.show_hide_pw_iv)
    private val pwEt = view.findViewById<EditText>(R.id.pw_et)

    init {
        pwIv.setOnClickListener {
            if (pwEt.transformationMethod == null) // visible
                hidePw()
            else
                showPw()
        }

        val etGroup = if (isLoggedIn)
            arrayOf(view.findViewById(R.id.acc_et), pwEt)
        else
            arrayOf(view.findViewById(R.id.email_et), view.findViewById(R.id.username_et), pwEt)

        for (et in etGroup) {
            et.addTextChangedListener { _ ->
                var allNotEmpty = etGroup.all { it.str.isNotEmpty() }
                if (!isLoggedIn)
                    allNotEmpty = allNotEmpty && etGroup[0].str.isEmail
                dialogPosBtn.isEnabled = allNotEmpty
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