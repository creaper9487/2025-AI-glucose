package rl.collab.aiglucose.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rl.collab.aiglucose.databinding.FragAccBinding
import rl.collab.aiglucose.onClick.LogInBtnOnClick
import rl.collab.aiglucose.onClick.RegisterBtnOnClick

class AccFrag : UniversalFrag() {
    private lateinit var binding: FragAccBinding

    companion object {
        var loggedIn = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragAccBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update()

        binding.registerBtn.setOnClickListener(RegisterBtnOnClick(this))
        binding.logInBtn.setOnClickListener(LogInBtnOnClick(this))
        binding.continueWithGoogleBtn.setOnClickListener {
            logIn()
        }

        binding.logOutBtn.setOnClickListener {
            logOut()
        }
    }

    fun logIn() {
        loggedIn = true
        update()
    }

    fun logOut() {
        loggedIn = false
        update()
    }

    override fun update() {
        if (loggedIn) {
            binding.accLy.visibility = View.INVISIBLE
            binding.profileLy.visibility = View.VISIBLE
        } else {
            binding.profileLy.visibility = View.INVISIBLE
            binding.accLy.visibility = View.VISIBLE
        }
    }
}