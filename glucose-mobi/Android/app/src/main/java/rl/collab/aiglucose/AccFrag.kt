package rl.collab.aiglucose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rl.collab.aiglucose.databinding.FragAccBinding

class AccFrag : UniversalFrag() {
    private lateinit var binding: FragAccBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragAccBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun update() {

    }
}