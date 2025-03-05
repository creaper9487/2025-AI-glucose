package rl.collab.diabeat.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import retrofit2.Response
import rl.collab.diabeat.Client.request
import rl.collab.diabeat.R
import rl.collab.diabeat.Result
import rl.collab.diabeat.databinding.FragChartBinding

class ChartFrag : Fragment() {
    private var _binding: FragChartBinding? = null
    private val binding get() = _binding!!

    companion object {
        var data = mutableListOf<Result.Records>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
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

            table.adapter = TableAdapter()
            swipeRefresh.setOnRefreshListener { reqRecords() }
        }
    }

    private fun reqRecords() {
        AccFrag.acc ?: return

        val onSucceed = { r: Response<List<Result.Records>> ->
            val rdata = r.body()!!
            val before = data.size
            val after = rdata.size
            data.addAll(rdata.drop(before))

            binding.swipeRefresh.isRefreshing = false
            binding.table.adapter!!.notifyDataSetChanged()
        }

        request(onSucceed, null, null, false) { getRecords(AccFrag.access!!) }
    }
}