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
import rl.collab.diabeat.Result
import rl.collab.diabeat.click.LoginBtnClick
import rl.collab.diabeat.click.RegisterBtnClick
import rl.collab.diabeat.databinding.FragAccBinding
import rl.collab.diabeat.shortToast
import java.io.File

class AccFrag : Fragment() {
    lateinit var binding: FragAccBinding
    lateinit var accFile: File
    lateinit var pwFile: File

    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val acc = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
            Toast.makeText(requireContext(), acc.email, Toast.LENGTH_SHORT).show()
            googleSignInClient.signOut()
        }
    }

    companion object {
        var token: Result.Token? = null
        var acc: String? = null
        var pw: String? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragAccBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accFile = File(requireContext().filesDir, "acc.txt")
        pwFile = File(requireContext().filesDir, "pw.txt")

        if (token == null && acc == null && pw == null)
            logOutEnv()
        else
            logInEnv(token!!, acc!!, pw!!)

        binding.googleSignInBtn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.token))
                .requestProfile()
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
        binding.registerBtn.setOnClickListener(RegisterBtnClick(this))
        binding.loginBtn.setOnClickListener(LoginBtnClick(this))
        binding.coffeeBtn.setOnClickListener {
            val uri = Uri.parse("https://github.com/creaper9487/2025-AI-glucose")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        binding.suggestBtn.setOnClickListener {
            it.isEnabled = false
            shortToast("TODO: max wait time 60s")
            Client.suggest(this)
        }
        binding.predictBtn.setOnClickListener {
            val obj = Request.Diabetes("male", 18, 1.0, 0, "never", 24.0, 5.7, 100)
            Client.predictDiabetes(this, obj)
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

    fun logInEnv(token: Result.Token, acc: String, pw: String) {
        AccFrag.token = token
        AccFrag.acc = acc
        AccFrag.pw = pw
        binding.accLy.visibility = View.INVISIBLE
        binding.profileLy.visibility = View.VISIBLE
        binding.profileTv.text = "Hi, $acc"

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
        token = null
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
