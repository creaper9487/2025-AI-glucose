package rl.collab.diabeat.frag

import android.view.View
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import rl.collab.diabeat.R
import rl.collab.diabeat.Result
import rl.collab.diabeat.databinding.FragChartBinding


class ChartFrag : MyFrag<FragChartBinding>(FragChartBinding::inflate) {
    private lateinit var adapter: MyAdapter

    override fun FragChartBinding.setView() {
        adapter = MyAdapter(layoutInflater, vm.records)
        table.adapter = adapter

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
            getRecords()
        }

        swipeRefresh.isEnabled = vm.acc != null
        swipeRefresh.setOnRefreshListener { getRecords() }
    }

    private fun getRecords() {
        vm.acc ?: return

        val onSucceed = { r: List<Result.Records> ->
            vm.records.clear()
            vm.records.addAll(r)

            if (binding.table.visibility == View.VISIBLE)
                adapter.notifyDataSetChanged()
            else {
                val entries = mutableListOf<Entry>()
                for ((i, it) in r.reversed().withIndex()) {
                    entries.add(Entry(i.toFloat(), it.blood_glucose.toFloat()))
                }

                binding.chart.data = LineData(LineDataSet(entries, ""))
                binding.chart.invalidate()
            }
            binding.swipeRefresh.isRefreshing = false
        }
        request(onSucceed, null, null, false) { getRecords(vm.access!!) }
    }
}