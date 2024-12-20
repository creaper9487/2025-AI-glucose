package rl.collab.aiglucose.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import rl.collab.aiglucose.R
import rl.collab.aiglucose.databinding.FragAccBinding
import rl.collab.aiglucose.onClick.LogInBtnOnClick
import rl.collab.aiglucose.onClick.RegisterBtnOnClick

class AccFrag : Fragment() {
    private lateinit var binding: FragAccBinding

    companion object {
        const val ACC_JSON = "acc.json"
        var loggedIn = false
    }

    val hasBio
        get(): Boolean {
            val bioMan = BiometricManager.from(requireContext())
            return bioMan.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragAccBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (loggedIn)
            logIn()
        else
            logOut()

        binding.registerBtn.setOnClickListener(RegisterBtnOnClick(this))
        binding.logInBtn.setOnClickListener(LogInBtnOnClick(this))
        binding.googleSignInBtn.setOnClickListener { logIn() }
        binding.logOutBtn.setOnClickListener { logOut() }
    }

    fun logIn() {
        loggedIn = true
        binding.accLy.visibility = View.INVISIBLE
        binding.profileLy.visibility = View.VISIBLE
    }

    private fun logOut() {
        loggedIn = false
        binding.profileLy.visibility = View.INVISIBLE
        binding.accLy.visibility = View.VISIBLE
    }

    fun bioAuth(onAuthSucceed: () -> Unit, onAuthFailed: () -> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.bio_log_in))
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSucceed()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onAuthFailed()
            }
        }

        val bioPrompt = BiometricPrompt(this, callback)

        bioPrompt.authenticate(promptInfo)
    }
}