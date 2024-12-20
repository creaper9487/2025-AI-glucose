package rl.collab.aiglucose.onClick

import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import rl.collab.aiglucose.R

class PwIvOnClick : View.OnClickListener {
    private lateinit var pwIv: ImageView
    private lateinit var pwEt: EditText

    override fun onClick(p0: View?) {
        val view = p0!!
        pwIv = view.findViewById(R.id.show_hide_pw_iv)
        pwEt = view.findViewById(R.id.pw_et)

        pwIv.setOnClickListener {
            if (pwEt.transformationMethod == null) // visible
                hide()
            else
                show()
        }
    }

    fun hide() {
        pwEt.transformationMethod = PasswordTransformationMethod.getInstance()
        pwIv.setImageResource(R.drawable.visible)
    }

    fun show() {
        pwEt.transformationMethod = null
        pwIv.setImageResource(R.drawable.invisible)
    }
}