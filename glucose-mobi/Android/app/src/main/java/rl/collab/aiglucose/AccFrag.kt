package rl.collab.aiglucose

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import rl.collab.aiglucose.databinding.FragAccBinding

class AccFrag : UniversalFrag() {
    private lateinit var binding: FragAccBinding
    private var pwVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragAccBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signInBtn.setOnClickListener {
            binding.signUpBtn.visibility = View.GONE
            binding.continueWithGoogleBtn.visibility = View.GONE
            showEtGroup()

            binding.emailEt.visibility = View.GONE
            binding.usernameEt.hint = getString(R.string.username_email)
            val constraintSet = ConstraintSet()
            constraintSet.apply {
                clone(binding.etGroup)
                connect(binding.usernameEt.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                applyTo(binding.etGroup)
            }
        }

        binding.signUpBtn.setOnClickListener {
            binding.signInBtn.visibility = View.GONE
            binding.continueWithGoogleBtn.visibility = View.GONE
            showEtGroup()
        }

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
    }

    private fun showEtGroup() {
        binding.etGroup.visibility = View.VISIBLE
        val constraintSet = ConstraintSet()
        constraintSet.apply {
            clone(binding.root)
            connect(binding.accTv.id, ConstraintSet.BOTTOM, binding.etGroup.id, ConstraintSet.TOP)
            connect(binding.btnGroup.id, ConstraintSet.TOP, binding.etGroup.id, ConstraintSet.BOTTOM)
            applyTo(binding.root)
        }
    }

    override fun update() {
    }
}