package rl.collab.aiglucose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rl.collab.aiglucose.databinding.FragFigureBinding

class FigureFrag : UniversalFrag() {
    private lateinit var binding: FragFigureBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragFigureBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun update() {

    }
}