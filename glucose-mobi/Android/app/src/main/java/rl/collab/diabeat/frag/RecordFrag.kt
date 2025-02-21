package rl.collab.diabeat.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import rl.collab.diabeat.Client
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.FragRecordBinding
import rl.collab.diabeat.onclick.PredictCarbohydrateBtnListener
import rl.collab.diabeat.str
import rl.collab.diabeat.toast

class RecordFrag : Fragment() {
    lateinit var binding: FragRecordBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveBtn.setOnClickListener {
            if (AccFrag.tokens == null)
                toast("請先登入")
            else if (binding.glucoseEt.str.isEmpty())
                toast("血糖值不能為空")
            else {
                val obj = Request.Record(
                    binding.glucoseEt.str.toDouble(),
                    binding.carbohydrateEt.str.toDoubleOrNull(),
                    binding.exerciseEt.str.toDoubleOrNull(),
                    binding.insulinEt.str.toDoubleOrNull()
                )
                Client.postRecord(this, obj)
            }
        }

        binding.predictCarbohydrateBtn.setOnClickListener(PredictCarbohydrateBtnListener(this))
    }
}