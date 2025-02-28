package rl.collab.diabeat.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import rl.collab.diabeat.R
import rl.collab.diabeat.databinding.FragChartBinding

class ChartFrag : Fragment() {
    private var _binding: FragChartBinding? = null
    private val binding get() = _binding!!

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

        binding.run {
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
        }
    }
}