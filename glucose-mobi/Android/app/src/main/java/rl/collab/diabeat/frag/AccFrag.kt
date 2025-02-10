package rl.collab.diabeat.frag

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import rl.collab.diabeat.Client
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.FragAccBinding
import rl.collab.diabeat.onclick.LoginBtnOnClick
import rl.collab.diabeat.onclick.RegisterBtnOnClick
import java.io.File

class AccFrag : Fragment() {
    private lateinit var binding: FragAccBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val acc = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
            Toast.makeText(requireContext(), acc.email, Toast.LENGTH_SHORT).show()
            googleSignInClient.signOut()
        }
    }

    val accFile by lazy { File(requireContext().filesDir, "acc.txt") }
    val pwFile by lazy { File(requireContext().filesDir, "pw.txt") }

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

        binding.googleSignInBtn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.token))
                .requestProfile()
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            resultLauncher.launch(googleSignInClient.signInIntent)
        }
        binding.registerBtn.setOnClickListener(RegisterBtnOnClick(this))
        binding.loginBtn.setOnClickListener(LoginBtnOnClick(this))
        binding.coffeeBtn.setOnClickListener {
            val uri = Uri.parse("https://github.com/creaper9487/2025-AI-glucose")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        binding.logOutBtn.setOnClickListener { logOutEnv() }
        binding.bioLoginSw.setOnCheckedChangeListener { _, isChecked ->
            if (binding.profileLy.visibility == View.VISIBLE) {
                if (isChecked) {
                    accFile.writeText(acc!!)
                    pwFile.writeText(pw!!)
                } else
                    pwFile.delete()
            }
        }
    }

    fun logInEnv(acc: String, pw: String) {
        AccFrag.acc = acc
        AccFrag.pw = pw
        binding.accLy.visibility = View.INVISIBLE
        binding.profileLy.visibility = View.VISIBLE

        val bioMan = BiometricManager.from(requireContext())
        if (bioMan.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            binding.bioLoginSw.isEnabled = false
        } else if (!pwFile.exists()) {
            binding.bioLoginSw.isChecked = false
            binding.bioLoginSw.jumpDrawablesToCurrentState()  // skip animation
        } else if (pwFile.exists()) {
            binding.bioLoginSw.isChecked = true
            binding.bioLoginSw.jumpDrawablesToCurrentState()  // skip animation
        }
    }

    private fun logOutEnv() {
        acc = null
        pw = null
        binding.profileLy.visibility = View.INVISIBLE
        binding.accLy.visibility = View.VISIBLE
    }

    fun bioLogIn(request: Request.LogIn, dialogDismiss: () -> Unit, reme: Boolean) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("生物辨識登入")
            .setNegativeButtonText("取消")
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
