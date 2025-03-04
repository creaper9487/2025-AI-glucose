package rl.collab.diabeat.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import retrofit2.Response
import rl.collab.diabeat.Client.cancelJob
import rl.collab.diabeat.Client.request
import rl.collab.diabeat.Client.retro
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
        cancelJob()
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

            table.adapter = TableAdapter(data)
            swipeRefresh.setOnRefreshListener {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    fun reqRecords() {
        AccFrag.acc ?: return

        val onSucceed = { r: Response<List<Result.Records>> ->
            data.addAll(r.body()!!.drop(data.size))
            Unit
        }

        request(this, onSucceed, null, null) {
            retro.getRecords(AccFrag.access!!)
        }
    }
}