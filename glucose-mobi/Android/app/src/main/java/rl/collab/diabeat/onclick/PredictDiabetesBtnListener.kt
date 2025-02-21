package rl.collab.diabeat.onclick

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import rl.collab.diabeat.Client
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.DialogDiabetesBinding
import rl.collab.diabeat.frag.AccFrag
import rl.collab.diabeat.str
import rl.collab.diabeat.viewDialog

class PredictDiabetesBtnListener(private val accFrag: AccFrag) : View.OnClickListener {

    override fun onClick(p0: View?) {
        val binding = DialogDiabetesBinding.inflate(accFrag.layoutInflater)

        binding.run {
            val ets = arrayOf(smokingHistoryAc, ageEt, bmiEt, hb1acEt, glucoseEt)
            smokingHistoryAc.setSimpleItems(
                arrayOf("從不吸菸", "曾經吸菸", "目前沒有吸菸", "目前有吸菸")
            )

            val dialog = accFrag.viewDialog("預測是否得糖尿病", root, null)

            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            posBtn.isEnabled = false
            posBtn.setOnClickListener {
                val obj = Request.Diabetes(
                    if (maleRb.isChecked) "male" else "female",
                    ageEt.str.toInt(),
                    hypertensionCb.isChecked,
                    heartDiseaseCb.isChecked,
                    smokingHistoryAc.str,
                    bmiEt.str.toDouble(),
                    hb1acEt.str.toDouble(),
                    glucoseEt.str.toInt()
                )
                Client.predictDiabetes(accFrag, obj)
            }
            val watcher = {
                posBtn.isEnabled = genderRg.checkedRadioButtonId != -1 && ets.all { it.str.isNotEmpty() }
            }
            genderRg.setOnCheckedChangeListener { _, _ -> watcher() }
            for (et in ets)
                et.doAfterTextChanged { watcher() }
        }
    }
}