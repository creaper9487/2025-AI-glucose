package rl.collab.diabeat.frag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import rl.collab.diabeat.R
import rl.collab.diabeat.Result
import rl.collab.diabeat.localDT
import rl.collab.diabeat.tryToInt

class TableAdapter(private val data: List<Result.Records>) : RecyclerView.Adapter<TableAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cellTv: TextView = view.findViewById(R.id.cell_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < 5)
            holder.cellTv.text = arrayOf("時間", "血糖", "碳水", "運動", "胰島素")[position % 5]
        else
            holder.cellTv.text = data[position / 5 - 1].run {
                arrayOf(
                    created_at.localDT,
                    blood_glucose.tryToInt,
                    carbohydrate_intake.tryToInt,
                    exercise_duration.tryToInt,
                    insulin_injection.tryToInt
                )[position % 5]
            }
    }

    override fun getItemCount() = (1 + data.size) * 5
}