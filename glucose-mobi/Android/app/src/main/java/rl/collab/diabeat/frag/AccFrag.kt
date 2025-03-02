package rl.collab.diabeat.frag

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.widget.doAfterTextChanged
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.fragment.app.Fragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import rl.collab.diabeat.Client
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.Result
import rl.collab.diabeat.bearer
import rl.collab.diabeat.databinding.DialogDiabetesBinding
import rl.collab.diabeat.databinding.DialogLoginBinding
import rl.collab.diabeat.databinding.DialogRegisterBinding
import rl.collab.diabeat.databinding.FragAccBinding
import rl.collab.diabeat.excDialog
import rl.collab.diabeat.hideKb
import rl.collab.diabeat.io
import rl.collab.diabeat.isEmail
import rl.collab.diabeat.nacho
import rl.collab.diabeat.pos
import rl.collab.diabeat.str
import rl.collab.diabeat.syncEdit
import rl.collab.diabeat.toast
import rl.collab.diabeat.ui
import rl.collab.diabeat.viewDialog

class AccFrag : Fragment() {
    private val credentialManager by lazy { CredentialManager.create(requireContext()) }
    private var _binding: FragAccBinding? = null
    private val binding get() = _binding!!

    private lateinit var remePref: SharedPreferences
    private val remeAcc get() = remePref.getString("acc", null)
    private val remeRefresh get() = remePref.getString("refresh", null)

    companion object {
        data class AuthData(
            val acc: String,
            val access: String,
            val refresh: String
        )

        fun authDataBuilder(acc: String, r: Result.Tokens) =
            AuthData(acc, r.access.bearer, r.refresh)

        fun authDataBuilder(r: Result.Refresh) =
            AuthData(r.username, r.access.bearer, r.refresh)

        var authData: AuthData? = null
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

        remePref = requireContext().getSharedPreferences("reme", Context.MODE_PRIVATE)
        authData?.also { logInEnv(null, null) } ?: logOutEnv()

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
                        putString("acc", authData!!.acc)
                        putString("refresh", authData!!.refresh)
                    } else
                        clear()
                }
            }
        }
    }

    fun logInEnv(authData: AuthData?, reme: Boolean?) {
        authData?.also {
            AccFrag.authData = authData
        }

        reme?.also {
            remePref.syncEdit {
                if (reme)
                    putString("acc", AccFrag.authData!!.acc)
                else {
                    remove("acc")
                    remove("refresh")
                }
            }
        }

        binding.apply {
            accLy.visibility = View.INVISIBLE
            profileLy.visibility = View.VISIBLE
            profileTv.text = "Hi, ${AccFrag.authData!!.acc}"

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

    private fun bioLogIn(refresh: String, dialog: AlertDialog, reme: Boolean) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("生物辨識登入")
            .setNegativeButtonText("取消")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Client.refresh(this@AccFrag, refresh, dialog, reme)
            }
        }

        val bioPrompt = BiometricPrompt(this, callback)
        bioPrompt.authenticate(promptInfo)
    }

    private fun logOutEnv() {
        authData = null
        binding.profileLy.visibility = View.INVISIBLE
        binding.accLy.visibility = View.VISIBLE
    }

    private fun googleSignInBtnOnClick() {
        val opt = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.token))
            .build()

        val obj = GetCredentialRequest.Builder()
            .addCredentialOption(opt)
            .build()

        io {
            try {
                val x = credentialManager.getCredential(requireContext(), obj)

                // 正確處理憑證類型
                if (x.credential is CustomCredential) {
                    val customCredential = x.credential as CustomCredential

                    // 檢查是否為 Google ID 類型
                    if (customCredential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        // 從 JSON 字符串解析 Google 憑證
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.data)

                        val email = googleIdTokenCredential.id
                        val idToken = googleIdTokenCredential.idToken
                        val name = googleIdTokenCredential.displayName

                        nacho("---start")
                        nacho(id)
                        nacho(idToken)
                        nacho(email)
                        nacho(name)

                        Client.retro.value.googleSignIn(Request.GoogleSignIn(idToken))


                        ui { toast("$email\n$name") }
                        nacho(idToken)
                    } else {
                        ui { toast("不支援的憑證類型") }
                    }
                } else {
                    ui { toast("無法獲取有效憑證") }
                }
            } catch (_: GetCredentialCancellationException) {
                // 用戶取消操作，不做處理
            } catch (e: Exception) {
                ui { excDialog(e) }
            }
        }
    }

    private fun registerBtnOnClick() {
        val binding = DialogRegisterBinding.inflate(layoutInflater)

        binding.apply {
            val dialog = viewDialog("註冊", root)
            val posBtn = dialog.pos
            posBtn.isEnabled = false
            posBtn.setOnClickListener {
                val obj = Request.Register(emailEt.str, usernameEt.str, pwEt.str)
                Client.register(this@AccFrag, obj, dialog)
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

        binding.apply {
            remeAcc?.also {
                accEt.setText(it)
                remeCb.isChecked = true
            }

            val dialog = viewDialog("登入", root, "生物辨識")
            val posBtn = dialog.pos
            posBtn.isEnabled = false
            posBtn.setOnClickListener {
                val obj = Request.Login(accEt.str.trim(), pwEt.str)
                Client.logIn(this@AccFrag, obj, dialog, remeCb.isChecked)
            }
            val watcher = { _: Editable? ->
                posBtn.isEnabled = accEt.str.isNotEmpty() && pwEt.str.isNotEmpty()
            }
            accEt.doAfterTextChanged(watcher)
            pwEt.doAfterTextChanged(watcher)
            pwEt.setOnEditorActionListener { _, _, _ ->
                pwEt.hideKb()
                posBtn.callOnClick()
            }

            val neutralBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            remeRefresh?.also {
                neutralBtn.setOnClickListener { _ ->
                    bioLogIn(it, dialog, remeCb.isChecked)
                }
                neutralBtn.callOnClick()
            } ?: run {
                neutralBtn.isEnabled = false
            }
        }
    }

    private fun predictDiabetesBtnOnClick() {
        val binding = DialogDiabetesBinding.inflate(layoutInflater)

        binding.apply {
            val ets = arrayOf(smokingHistoryAc, ageEt, bmiEt, hb1acEt, glucoseEt)
            smokingHistoryAc.setSimpleItems(
                arrayOf("從不吸菸", "曾經吸菸", "目前沒有吸菸", "目前有吸菸")
            )

            val dialog = viewDialog("預測是否得糖尿病", root)
            val posBtn = dialog.pos
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