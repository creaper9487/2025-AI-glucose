package rl.collab.diabeat.frag

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import rl.collab.diabeat.Result
import rl.collab.diabeat.databinding.FragAccBinding
import rl.collab.diabeat.onclick.LoginBtnListener
import rl.collab.diabeat.onclick.PredictDiabetesBtnListener
import rl.collab.diabeat.onclick.RegisterBtnListener
import rl.collab.diabeat.toast
import java.io.File

class AccFrag : Fragment() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: FragAccBinding
    lateinit var accFile: File
    lateinit var pwFile: File

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val acc = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
            toast(acc.email!!)
            googleSignInClient.signOut()
        }
    }

    companion object {
        var tokens: Result.Tokens? = null
        var acc: String? = null
        var pw: String? = null

        val key get() = "Bearer ${tokens!!.access}"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragAccBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accFile = File(requireContext().filesDir, "acc.txt")
        pwFile = File(requireContext().filesDir, "pw.txt")

        if (tokens == null && acc == null && pw == null)
            logOutEnv()
        else
            logInEnv(tokens!!, acc!!, pw!!)

        binding.run {
            googleSignInBtn.setOnClickListener {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.token))
                    .requestProfile()
                    .requestEmail()
                    .build()

                googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
            registerBtn.setOnClickListener(RegisterBtnListener(this@AccFrag))
            loginBtn.setOnClickListener(LoginBtnListener(this@AccFrag))
            aboutUsBtn.setOnClickListener {
                val uri = Uri.parse("https://github.com/creaper9487/2025-AI-glucose")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

            suggestBtn.setOnClickListener {
                Client.suggest(this@AccFrag)
            }
            predictDiabetesBtn.setOnClickListener(PredictDiabetesBtnListener(this@AccFrag))
            logOutBtn.setOnClickListener { logOutEnv() }
            bioLoginSw.setOnCheckedChangeListener { _, isChecked ->
                if (profileLy.visibility == View.VISIBLE) {
                    if (isChecked) {
                        accFile.writeText(acc!!)
                        pwFile.writeText(pw!!)
                    } else
                        pwFile.delete()
                }
            }
        }
    }

    fun logInEnv(tokens: Result.Tokens, acc: String, pw: String) {
        AccFrag.tokens = tokens
        AccFrag.acc = acc
        AccFrag.pw = pw

        binding.run {
            accLy.visibility = View.INVISIBLE
            profileLy.visibility = View.VISIBLE
            profileTv.text = "Hi, $acc"

            val bioMan = BiometricManager.from(requireContext())
            val canAuth = bioMan.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                bioLoginSw.isEnabled = false
            } else if (!pwFile.exists()) {
                bioLoginSw.isChecked = false
                bioLoginSw.jumpDrawablesToCurrentState()  // skip animation
            } else if (pwFile.exists()) {
                bioLoginSw.isChecked = true
                bioLoginSw.jumpDrawablesToCurrentState()  // skip animation
            }
        }
    }

    private fun logOutEnv() {
        tokens = null
        acc = null
        pw = null
        binding.profileLy.visibility = View.INVISIBLE
        binding.accLy.visibility = View.VISIBLE
    }

    fun bioLogIn(obj: Request.Login, dialogDismiss: () -> Unit, reme: Boolean) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("生物辨識登入")
            .setNegativeButtonText("取消")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Client.logIn(this@AccFrag, obj, dialogDismiss, reme)
            }
        }

        val bioPrompt = BiometricPrompt(this, callback)
        bioPrompt.authenticate(promptInfo)
    }
}