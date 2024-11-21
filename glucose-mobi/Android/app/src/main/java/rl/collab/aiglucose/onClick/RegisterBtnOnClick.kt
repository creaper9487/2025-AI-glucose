package rl.collab.aiglucose.onClick

import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat.getString
import androidx.core.widget.addTextChangedListener
import rl.collab.aiglucose.R
import rl.collab.aiglucose.customDialog
import rl.collab.aiglucose.date
import rl.collab.aiglucose.fgColor
import rl.collab.aiglucose.frag.AccFrag
import rl.collab.aiglucose.isAlNum
import rl.collab.aiglucose.isEmail
import rl.collab.aiglucose.setPosBtnOnClick
import rl.collab.aiglucose.str

class RegisterBtnOnClick(private val accFrag: AccFrag) : View.OnClickListener {

    override fun onClick(p0: View?) {
        val view = View.inflate(accFrag.con, R.layout.dialog_register, null)
        ShowHidePwIvOnClick().onClick(view)

        val errTv = view.findViewById<TextView>(R.id.err_tv)
        val emailEt = view.findViewById<EditText>(R.id.email_et)
        val uidEt = view.findViewById<EditText>(R.id.uid_et)
        val pwEt = view.findViewById<EditText>(R.id.pw_et)

        errTv.text = date
        uidEt.addTextChangedListener {
            if (uidEt.str.isAlNum) {
                errTv.text = date
                uidEt.setTextColor(accFrag.con.fgColor)
            } else {
                errTv.text = getString(accFrag.con, R.string.invalid_uid)
                uidEt.setTextColor(Color.RED)
            }
        }

        val dialog = accFrag.con.customDialog(R.string.register, view)
        dialog.setPosBtnOnClick {
            val errMsgs = mutableListOf<String>()
            if (!emailEt.str.isEmail)
                errMsgs.add(getString(accFrag.con, R.string.invalid_email))
            if (uidEt.text.isEmpty())
                errMsgs.add(getString(accFrag.con, R.string.invalid_uid))
            if (pwEt.text.isEmpty())
                errMsgs.add(getString(accFrag.con, R.string.invalid_pw))

            if (errMsgs.isEmpty()) {
                dialog.dismiss()
                accFrag.logIn()
            } else
                errTv.text = errMsgs.joinToString("\n")
        }
    }
}