package rl.collab.aiglucose.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rl.collab.aiglucose.databinding.FragProfileBinding

class ProfileFrag : UniversalFrag() {
    private lateinit var binding: FragProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun update() {
    }
}