package rl.collab.aiglucose.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rl.collab.aiglucose.MainActivity
import rl.collab.aiglucose.R
import rl.collab.aiglucose.databinding.FragAccBinding
import rl.collab.aiglucose.tt

class AccFrag : UniversalFrag() {
    private lateinit var binding: FragAccBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragAccBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerBtn.setOnClickListener {
            (context as MainActivity).accNavigate(R.id.registerFrag)
        }
        binding.loginBtn.setOnClickListener {
            (context as MainActivity).accNavigate(R.id.loginFrag)
        }
        binding.continueWithGoogleBtn.setOnClickListener {
            tt(requireContext(), "continue with google")
        }
    }

    override fun update() {
    }
}