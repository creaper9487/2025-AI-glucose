package rl.collab.diabeat.frag

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import rl.collab.diabeat.Client
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.FragAccBinding
import rl.collab.diabeat.onClick.LogInBtnOnClick
import rl.collab.diabeat.onClick.RegisterBtnOnClick
import java.io.File

class AccFrag : Fragment() {
    private lateinit var binding: FragAccBinding
    val accFile by lazy {
        File(requireContext().filesDir, "acc.txt")
    }
    val pwFile by lazy {
        File(requireContext().filesDir, "pw.txt")
    }

    private val resultLauncher = registerForActivityResult(StartActivityForResult()) {}

    companion object {
        var acc: String? = null
        var pw: String? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragAccBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (acc == null && pw == null)
            logOutEnv()
        else
            logInEnv(acc!!, pw!!)

        binding.registerBtn.setOnClickListener(RegisterBtnOnClick(this))
        binding.logInBtn.setOnClickListener(LogInBtnOnClick(this))
        binding.coffeeBtn.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/creaper9487/2025-AI-glucose")))
        }
        binding.googleSignInBtn.setOnClickListener {
            val gOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val gClient = GoogleSignIn.getClient(requireActivity(), gOption)
            resultLauncher.launch(gClient.signInIntent)
        }

        binding.logOutBtn.setOnClickListener { logOutEnv() }
        binding.bioLogInSw.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                accFile.writeText(acc!!)
                pwFile.writeText(pw!!)
            } else
                pwFile.delete()
        }
    }

    fun logInEnv(acc: String, pw: String) {
        AccFrag.acc = acc
        AccFrag.pw = pw
        binding.accLy.visibility = View.INVISIBLE

        val bioMan = BiometricManager.from(requireContext())
        if (bioMan.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS)
            binding.bioLogInSw.isEnabled = false
        else if (!pwFile.exists()) {
            binding.bioLogInSw.isChecked = false
            binding.bioLogInSw.jumpDrawablesToCurrentState()  // skip animation
        } else if (pwFile.exists()) {
            binding.bioLogInSw.isChecked = true
            binding.bioLogInSw.jumpDrawablesToCurrentState()  // skip animation
        }

        binding.profileLy.visibility = View.VISIBLE
    }

    private fun logOutEnv() {
        acc = null
        pw = null
        binding.profileLy.visibility = View.INVISIBLE
        binding.accLy.visibility = View.VISIBLE
    }

    fun bioLogIn(request: Request.LogIn, dialogDismiss: () -> Unit, reme: Boolean) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.bio_log_in))
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Client.logIn(this@AccFrag, request, dialogDismiss, reme)
            }
        }

        val bioPrompt = BiometricPrompt(this, callback)
        bioPrompt.authenticate(promptInfo)
    }
}