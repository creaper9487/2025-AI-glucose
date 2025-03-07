package rl.collab.diabeat.frag

import android.view.View
import rl.collab.diabeat.R
import rl.collab.diabeat.Result
import rl.collab.diabeat.databinding.FragChartBinding

class ChartFrag : MyFrag<FragChartBinding>(FragChartBinding::inflate) {

    override fun FragChartBinding.setView() {
        segBtnGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked)
                return@addOnButtonCheckedListener

            if (checkedId == R.id.chart_btn) {
                table.visibility = View.INVISIBLE
                chart.visibility = View.VISIBLE
            } else {
                chart.visibility = View.INVISIBLE
                table.visibility = View.VISIBLE
            }
        }

        swipeRefresh.isEnabled = vm.acc != null
        swipeRefresh.setOnRefreshListener { reqRecords() }
    }

    private fun reqRecords() {
        val onSucceed = { _: List<Result.Records> ->
            binding.swipeRefresh.isRefreshing = false
        }
        request(onSucceed, null, null, false) { getRecords(vm.access!!) }
    }
}