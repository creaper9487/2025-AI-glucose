package rl.collab.diabeat.frag

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import rl.collab.diabeat.Client
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.Result
import rl.collab.diabeat.databinding.DialogDiabetesBinding
import rl.collab.diabeat.databinding.DialogLoginBinding
import rl.collab.diabeat.databinding.DialogRegisterBinding
import rl.collab.diabeat.databinding.FragAccBinding
import rl.collab.diabeat.isEmail
import rl.collab.diabeat.str
import rl.collab.diabeat.toast
import rl.collab.diabeat.viewDialog
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
            registerBtn.setOnClickListener { registerBtnOnClick() }
            loginBtn.setOnClickListener { logInBtnOnClick() }
            aboutUsBtn.setOnClickListener {
                val uri = Uri.parse("https://github.com/creaper9487/2025-AI-glucose")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

            suggestBtn.setOnClickListener { Client.suggest(this@AccFrag) }
            predictDiabetesBtn.setOnClickListener { predictDiabetesBtnOnClick() }
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

    private fun bioLogIn(obj: Request.Login, dialogDismiss: () -> Unit, reme: Boolean) {
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

    private fun logOutEnv() {
        tokens = null
        acc = null
        pw = null
        binding.profileLy.visibility = View.INVISIBLE
        binding.accLy.visibility = View.VISIBLE
    }

    private fun registerBtnOnClick() {
        val binding = DialogRegisterBinding.inflate(layoutInflater)

        binding.run {
            val dialog = viewDialog("註冊", root, null)

            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            posBtn.isEnabled = false
            posBtn.setOnClickListener {
                val obj = Request.Register(emailEt.str, usernameEt.str, pwEt.str)
                Client.register(this@AccFrag, obj, dialog::dismiss)
            }
            val watcher = { _: Editable? ->
                posBtn.isEnabled = emailEt.str.isEmail && usernameEt.str.isNotEmpty() && pwEt.str.isNotEmpty()
            }
            emailEt.doAfterTextChanged(watcher)
            usernameEt.doAfterTextChanged(watcher)
            pwEt.doAfterTextChanged(watcher)
        }
    }

    private fun logInBtnOnClick() {
        val binding = DialogLoginBinding.inflate(layoutInflater)

        binding.run {
            if (accFile.exists()) {
                accEt.setText(accFile.readText())
                remeCb.isChecked = true
            }

            val dialog = viewDialog("登入", root, "生物辨識")
            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            posBtn.isEnabled = false
            posBtn.setOnClickListener {
                val obj = Request.Login(accEt.str.trim(), pwEt.str)
                Client.logIn(this@AccFrag, obj, dialog::dismiss, remeCb.isChecked)
            }
            val watcher = { _: Editable? ->
                posBtn.isEnabled = accEt.str.isNotEmpty() && pwEt.str.isNotEmpty()
            }
            accEt.doAfterTextChanged(watcher)
            pwEt.doAfterTextChanged(watcher)
            pwEt.setOnEditorActionListener { _, _, _ ->
                posBtn.callOnClick()
                val imm = pwEt.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(pwEt.windowToken, 0)
                true
            }

            val neutralBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            if (pwFile.exists()) {
                neutralBtn.setOnClickListener {
                    val obj = Request.Login(accFile.readText(), pwFile.readText())
                    bioLogIn(obj, dialog::dismiss, remeCb.isChecked)
                }
                neutralBtn.callOnClick()
            } else
                neutralBtn.isEnabled = false
        }
    }

    private fun predictDiabetesBtnOnClick() {
        val binding = DialogDiabetesBinding.inflate(layoutInflater)

        binding.run {
            val ets = arrayOf(smokingHistoryAc, ageEt, bmiEt, hb1acEt, glucoseEt)
            smokingHistoryAc.setSimpleItems(
                arrayOf("從不吸菸", "曾經吸菸", "目前沒有吸菸", "目前有吸菸")
            )

            val dialog = viewDialog("預測是否得糖尿病", root, null)

            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            posBtn.isEnabled = false
            posBtn.setOnClickListener {
                val obj = Request.Diabetes(
                    if (maleRb.isChecked) "male" else "female",
                    ageEt.str.toInt(),
                    hypertensionCb.isChecked,
                    heartDiseaseCb.isChecked,
                    smokingHistoryAc.str,
                    bmiEt.str.toDouble(),
                    hb1acEt.str.toDouble(),
                    glucoseEt.str.toInt()
                )
                Client.predictDiabetes(this@AccFrag, obj)
            }
            val watcher = {
                posBtn.isEnabled = genderRg.checkedRadioButtonId != -1 && ets.all { it.str.isNotEmpty() }
            }
            genderRg.setOnCheckedChangeListener { _, _ -> watcher() }
            for (et in ets)
                et.doAfterTextChanged { watcher() }
        }
    }
}