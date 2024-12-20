package rl.collab.aiglucose.frag

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import rl.collab.aiglucose.databinding.FragFigureBinding

class FigureFrag : Fragment() {
    private lateinit var binding: FragFigureBinding
    private lateinit var lineChart: LineChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragFigureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lineChart = binding.lineChart

        val entries = arrayListOf(0f en 1f, 1f en 2f, 2f en 0f, 3f en 4f, 4f en 5f)
        val dataSet = LineDataSet(entries, "Sample Data").apply {
            color = Color.GREEN
            valueTextColor = Color.BLACK
            lineWidth = 2f
        }

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }

    private infix fun Float.en(that: Float) = Entry(this, that)
}