package rl.collab.aiglucose.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rl.collab.aiglucose.R
import rl.collab.aiglucose.databinding.FragAccBinding
import rl.collab.aiglucose.setPosBtnOnClick

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
        setAccLy()
        setProfileLy()
    }

    private fun setAccLy() {
        binding.registerBtn.setOnClickListener {
            val registerView = layoutInflater.inflate(R.layout.dialog_register, null)
            ShowHidePwIvOnClick().onClick(registerView)

            val dialog = ma.customDialog(R.string.register, registerView)
            dialog.setPosBtnOnClick {
                dialog.dismiss()
                logIn()
            }
        }

        binding.logInBtn.setOnClickListener {
            val loginView = layoutInflater.inflate(R.layout.dialog_login, null)
            ShowHidePwIvOnClick().onClick(loginView)

            val dialog = ma.customDialog(R.string.log_in, loginView)
            dialog.setPosBtnOnClick {
                dialog.dismiss()
                logIn()
            }
        }

        binding.continueWithGoogleBtn.setOnClickListener {
            logIn()
        }
    }

    private fun setProfileLy() {
        binding.logOutBtn.setOnClickListener {
            logOut()
        }
    }

    private fun logIn() {
        loggedIn = true
        update()
    }

    private fun logOut() {
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