package rl.collab.diabeat.frag

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.widget.doAfterTextChanged
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.Fragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import rl.collab.diabeat.Client
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.databinding.DialogDiabetesInBinding
import rl.collab.diabeat.databinding.DialogLoginBinding
import rl.collab.diabeat.databinding.DialogRegisterBinding
import rl.collab.diabeat.databinding.FragAccBinding
import rl.collab.diabeat.exceptionDialog
import rl.collab.diabeat.io
import rl.collab.diabeat.nacho
import rl.collab.diabeat.neutral
import rl.collab.diabeat.pos
import rl.collab.diabeat.setCancelJob
import rl.collab.diabeat.str
import rl.collab.diabeat.syncEdit
import rl.collab.diabeat.toast
import rl.collab.diabeat.ui
import rl.collab.diabeat.viewDialog

class AccFrag : Fragment() {
    private val credentialManager by lazy { CredentialManager.create(requireContext()) }
    private var _binding: FragAccBinding? = null
    private val binding get() = _binding!!

    companion object {
        lateinit var remePref: SharedPreferences
        private val remeAcc get() = remePref.getString("acc", null)
        private val remeRefresh get() = remePref.getString("refresh", null)

        var acc: String? = null
        var access: String? = null
        var refresh: String? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragAccBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        acc?.also { logInEnv(null) } ?: logOutEnv()

        binding.apply {
            googleSignInBtn.setOnClickListener { googleSignInBtnOnClick() }
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
                if (profileLy.visibility != View.VISIBLE)
                    return@setOnCheckedChangeListener

                remePref.syncEdit {
                    if (isChecked) {
                        putString("acc", acc)
                        putString("refresh", refresh)
                    } else
                        clear()
                }
            }
        }
    }

    fun logInEnv(reme: Boolean?, pAcc: String? = null, pAccessRaw: String? = null, pRefresh: String? = null) {
        pAcc?.also {
            acc = pAcc
            access = "Bearer $pAccessRaw"
            refresh = pRefresh
        }

        reme?.also {
            remePref.syncEdit {
                if (reme)
                    putString("acc", acc)
                else {
                    remove("acc")
                    remove("refresh")
                }
            }
        }

        binding.apply {
            accLy.visibility = View.INVISIBLE
            profileLy.visibility = View.VISIBLE
            profileTv.text = "Hi, $acc"

            val bioMan = BiometricManager.from(requireContext())
            val canAuth = bioMan.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            if (canAuth != BiometricManager.BIOMETRIC_SUCCESS)
                bioLoginSw.isEnabled = false
            else {
                bioLoginSw.isChecked = remeRefresh != null
                bioLoginSw.jumpDrawablesToCurrentState()
            }
        }
    }

    private fun bioLogIn(dialog: AlertDialog, reme: Boolean) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("生物辨識登入")
            .setNegativeButtonText("取消")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Client.refresh(this@AccFrag, remeRefresh, dialog, reme)
            }
        }

        val bioPrompt = BiometricPrompt(this, callback)
        bioPrompt.authenticate(promptInfo)
    }

    private fun logOutEnv() {
        acc = null
        access = null
        refresh = null
        binding.profileLy.visibility = View.INVISIBLE
        binding.accLy.visibility = View.VISIBLE
    }

    private fun googleSignInBtnOnClick() {
        val opt = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.token))
            .build()

        val credentialObj = GetCredentialRequest.Builder()
            .addCredentialOption(opt)
            .build()

        io {
            try {
                val credentialResponse = credentialManager.getCredential(requireContext(), credentialObj)

                val credential = credentialResponse.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                    val email = googleIdTokenCredential.id
                    val idToken = googleIdTokenCredential.idToken
                    val name = googleIdTokenCredential.displayName

                    val obj = Request.GoogleSignIn(idToken)
                    Client.googleSignIn(this@AccFrag, obj)

                    ui { toast("$email\n$name") }
                    nacho(idToken)
                } else {
                    ui { toast("無法獲取有效憑證") }
                }
            } catch (e: GetCredentialCancellationException) {
                // 用戶取消操作，可以選擇不處理或是添加適當的提示
            } catch (e: NoCredentialException) {
                // 明確處理沒有可用憑證的情況
                ui { toast("沒有找到可用的憑證") }
            } catch (e: Exception) {
                ui { exceptionDialog(e) }
            }
        }
    }

    private fun registerBtnOnClick() {
        val binding = DialogRegisterBinding.inflate(layoutInflater)

        binding.apply {
            val dialog = viewDialog("註冊", root)

            dialog.pos.apply {
                isEnabled = false
                setOnClickListener {
                    isEnabled = false
                    val obj = Request.Register(emailEt.str, usernameEt.str, pwEt.str)
                    Client.register(this@AccFrag, obj, dialog)
                }
                val watcher = { _: Editable? ->
                    isEnabled = Patterns.EMAIL_ADDRESS.matcher(emailEt.str).matches() &&
                            usernameEt.str.isNotEmpty() &&
                            pwEt.str.isNotEmpty()
                }
                emailEt.doAfterTextChanged(watcher)
                usernameEt.doAfterTextChanged(watcher)
                pwEt.doAfterTextChanged(watcher)
            }

            dialog.setCancelJob()
        }
    }

    private fun logInBtnOnClick() {
        val binding = DialogLoginBinding.inflate(layoutInflater)

        binding.apply {
            remeAcc?.also {
                accEt.setText(it)
                remeCb.isChecked = true
            }

            val dialog = viewDialog("登入", root, neutral = "生物辨識")

            dialog.pos.apply {
                isEnabled = false
                setOnClickListener {
                    isEnabled = false
                    val obj = Request.Login(accEt.str.trim(), pwEt.str)
                    Client.logIn(this@AccFrag, obj, dialog, remeCb.isChecked)
                }
                val watcher = { _: Editable? ->
                    isEnabled = accEt.str.isNotEmpty() && pwEt.str.isNotEmpty()
                }
                accEt.doAfterTextChanged(watcher)
                pwEt.doAfterTextChanged(watcher)
                pwEt.setOnEditorActionListener { _, _, _ ->
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(pwEt.windowToken, 0)
                    callOnClick()
                }
            }

            dialog.setCancelJob()

            dialog.neutral.apply {
                if (remeRefresh != null) {
                    setOnClickListener { _ ->
                        bioLogIn(dialog, remeCb.isChecked)
                    }
                    callOnClick()
                } else
                    isEnabled = false
            }
        }
    }

    private fun predictDiabetesBtnOnClick() {
        val binding = DialogDiabetesInBinding.inflate(layoutInflater)

        binding.apply {
            val ets = arrayOf(smokingHistoryAc, ageEt, bmiEt, hb1acEt, glucoseEt)
            val simpleItems = arrayOf("從不吸菸", "曾經吸菸", "目前沒有吸菸", "目前有吸菸")
            val objItems = arrayOf("never", "former", "not current", "current")
            smokingHistoryAc.setSimpleItems(simpleItems)

            val dialog = viewDialog("預測是否有糖尿病", root)

            dialog.pos.apply {
                isEnabled = false
                setOnClickListener {
                    val obj = Request.Diabetes(
                        if (maleRb.isChecked) "male" else "female",
                        ageEt.str.toInt(),
                        hypertensionCb.isChecked,
                        heartDiseaseCb.isChecked,
                        objItems[simpleItems.indexOf(smokingHistoryAc.str)],
                        bmiEt.str.toDouble(),
                        hb1acEt.str.toDouble(),
                        glucoseEt.str.toInt()
                    )
                    Client.predictDiabetes(this@AccFrag, obj, dialog, simpleItems, objItems)
                }
                val watcher = {
                    isEnabled = genderRg.checkedRadioButtonId != -1 && ets.all { it.str.isNotEmpty() }
                }
                genderRg.setOnCheckedChangeListener { _, _ -> watcher() }
                for (et in ets)
                    et.doAfterTextChanged { watcher() }
            }

            dialog.setCancelJob()
        }
    }
}