package rl.collab.aiglucose.frag

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import rl.collab.aiglucose.MainActivity
import rl.collab.aiglucose.R
import rl.collab.aiglucose.databinding.FragLoginBinding

class LoginFrag : UniversalFrag() {
    private lateinit var binding: FragLoginBinding
    private var pwVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.showHidePwIv.setOnClickListener {
            if (pwVisible) {
                binding.pwEt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.showHidePwIv.setImageResource(R.drawable.invisible)
                pwVisible = false
            } else {
                binding.pwEt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.showHidePwIv.setImageResource(R.drawable.visible)
                pwVisible = true
            }
        }
        binding.loginBtn.setOnClickListener {
            findNavController().popBackStack()
            (context as MainActivity).accNavigate(R.id.profileFrag)
        }
    }

    override fun update() {
    }
}