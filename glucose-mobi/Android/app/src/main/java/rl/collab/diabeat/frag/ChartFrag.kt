package rl.collab.diabeat.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import rl.collab.diabeat.Client
import rl.collab.diabeat.Result
import rl.collab.diabeat.databinding.FragChartBinding

class ChartFrag : Fragment() {
    private lateinit var binding: FragChartBinding
    lateinit var table: RecyclerView
    val data = mutableListOf<Result.Records>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        table = binding.table
        table.layoutManager = GridLayoutManager(requireContext(), 5)
        table.adapter = TableAdapter(data)
        Client.getRecords(this)
    }
}