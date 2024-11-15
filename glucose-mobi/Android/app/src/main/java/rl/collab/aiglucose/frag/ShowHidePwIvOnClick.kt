package rl.collab.aiglucose.frag

import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import rl.collab.aiglucose.R

class ShowHidePwIvOnClick : View.OnClickListener {

    override fun onClick(view: View) {

        val showHidePwIv = view.findViewById<ImageView>(R.id.show_hide_pw_iv)
        val pwEt = view.findViewById<EditText>(R.id.pw_et)

        val visibleType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        val invisibleType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        showHidePwIv.setOnClickListener {
            if (pwEt.inputType == invisibleType) {
                pwEt.inputType = visibleType
                showHidePwIv.setImageResource(R.drawable.visible)
            } else {
                pwEt.inputType = invisibleType
                showHidePwIv.setImageResource(R.drawable.invisible)
            }
        }
    }
}