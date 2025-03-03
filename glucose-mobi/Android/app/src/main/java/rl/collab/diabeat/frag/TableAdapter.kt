package rl.collab.diabeat.frag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import rl.collab.diabeat.R

class TableAdapter(private val data: List<Result.Records>) : RecyclerView.Adapter<TableAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cellTv: MaterialTextView = view.findViewById(R.id.cell_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cellTv.text =
            if (position < 5)
                arrayOf("時間", "血糖", "碳水", "運動", "胰島素")[position % 5]
            else
                data[position / 5 - 1].run {
                    arrayOf(
                        created_at,
                        blood_glucose.tryToInt(),
                        carbohydrate_intake.tryToInt(),
                        exercise_duration.tryToInt(),
                        insulin_injection.tryToInt()
                    )[position % 5]
                }
    }

    override fun getItemCount() = (1 + data.size) * 5

    private fun Double?.tryToInt() =
        if (this == null)
            "-"
        else if (this % 1.0 == 0.0)
            toInt().toString()
        else
            toString()
}