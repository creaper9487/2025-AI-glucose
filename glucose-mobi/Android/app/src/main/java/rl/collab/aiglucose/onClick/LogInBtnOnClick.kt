package rl.collab.aiglucose.onClick

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat.getString
import rl.collab.aiglucose.R
import rl.collab.aiglucose.customDialog
import rl.collab.aiglucose.date
import rl.collab.aiglucose.frag.AccFrag
import rl.collab.aiglucose.setPosBtnOnClick

class LogInBtnOnClick(private val accFrag: AccFrag) : View.OnClickListener {

    override fun onClick(p0: View?) {
        val view = View.inflate(accFrag.con, R.layout.dialog_login, null)
        ShowHidePwIvOnClick().onClick(view)

        val errTv = view.findViewById<TextView>(R.id.err_tv)
        val accEt = view.findViewById<EditText>(R.id.acc_et)
        val pwEt = view.findViewById<EditText>(R.id.pw_et)

        errTv.text = date

        val dialog = accFrag.con.customDialog(R.string.log_in, view)
        dialog.setPosBtnOnClick {
            val errMsgs = mutableListOf<String>()
            if (accEt.text.isEmpty())
                errMsgs.add(getString(accFrag.con, R.string.invalid_acc))
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