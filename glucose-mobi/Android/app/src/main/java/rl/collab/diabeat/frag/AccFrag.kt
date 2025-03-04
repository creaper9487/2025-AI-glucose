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
import android.widget.Button
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
import kotlinx.coroutines.delay
import retrofit2.Response
import rl.collab.diabeat.Client
import rl.collab.diabeat.Client.cancelJob
import rl.collab.diabeat.Client.gson
import rl.collab.diabeat.Client.request
import rl.collab.diabeat.Client.retro
import rl.collab.diabeat.Client.retroLong
import rl.collab.diabeat.Err
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.Result
import rl.collab.diabeat.databinding.DialogDiabetesInBinding
import rl.collab.diabeat.databinding.DialogDiabetesOutBinding
import rl.collab.diabeat.databinding.DialogLoginBinding
import rl.collab.diabeat.databinding.DialogRegisterBinding
import rl.collab.diabeat.databinding.FragAccBinding
import rl.collab.diabeat.dialog
import rl.collab.diabeat.exceptionDialog
import rl.collab.diabeat.io
import rl.collab.diabeat.nacho
import rl.collab.diabeat.neg
import rl.collab.diabeat.neutral
import rl.collab.diabeat.pos
import rl.collab.diabeat.str
import rl.collab.diabeat.syncEdit
import rl.collab.diabeat.toast
import rl.collab.diabeat.ui
import rl.collab.diabeat.viewDialog
import java.util.concurrent.atomic.AtomicBoolean

class AccFrag : Fragment() {
    private var _binding: FragAccBinding? = null
    private val binding get() = _binding!!
    private val credentialManager by lazy { CredentialManager.create(requireContext()) }

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
        cancelJob()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            googleSignInBtn.setOnClickListener { clkGoogleSignIn() }
            registerBtn.setOnClickListener { clkRegistration() }
            loginBtn.setOnClickListener { clkLogin() }
            aboutUsBtn.setOnClickListener {
                val uri = Uri.parse("https://github.com/creaper9487/2025-AI-glucose")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

            suggestBtn.setOnClickListener { reqSuggestion() }
            predictDiabetesBtn.setOnClickListener { clkPredictDiabetes() }
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
        acc?.also { logInEnv(null) } ?: logOutEnv()
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

    //
    //
    // Click

    private fun AlertDialog.setCancelJob(btn: Button) =
        btn.setOnClickListener {
            cancelJob()
            dismiss()
        }

    private fun clkGoogleSignIn() {
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
                    reqGoogleSignIn(obj)

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

    private fun clkRegistration() {
        val binding = DialogRegisterBinding.inflate(layoutInflater)

        binding.apply {
            val dialog = viewDialog("註冊", root)

            dialog.pos.apply {
                isEnabled = false
                setOnClickListener {
                    isEnabled = false
                    val obj = Request.Register(emailEt.str, usernameEt.str, pwEt.str)
                    reqRegistration(obj, dialog)
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

            dialog.setCancelJob(dialog.neg)
        }
    }

    private fun clkLogin() {
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
                    reqLogin(obj, dialog, remeCb.isChecked)
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

            dialog.setCancelJob(dialog.neg)

            dialog.neutral.apply {
                if (remeRefresh == null)
                    isEnabled = false
                else {
                    setOnClickListener { _ ->
                        bioLogIn(dialog, remeCb.isChecked)
                    }
                    callOnClick()
                }
            }
        }
    }

    private fun clkPredictDiabetes() {
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
                    reqPredictDiabetes(obj, dialog, simpleItems, objItems)
                }
                val watcher = {
                    isEnabled = genderRg.checkedRadioButtonId != -1 && ets.all { it.str.isNotEmpty() }
                }
                genderRg.setOnCheckedChangeListener { _, _ -> watcher() }
                for (et in ets)
                    et.doAfterTextChanged { watcher() }
            }

            dialog.setCancelJob(dialog.neg)
        }
    }

    //
    //
    // Request

    private fun share(content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))
    }

    private fun reqGoogleSignIn(obj: Request.GoogleSignIn) {
        val onSucceed = { _: Response<Unit> ->
        }

        request(this, onSucceed, null, null) {
            retro.googleSignIn(obj)
        }
    }

    private fun reqRegistration(obj: Request.Register, dialog: AlertDialog) {
        val onSucceed = { r: Response<Result.Tokens> ->
            dialog.dismiss()
            val rr = r.body()!!
            logInEnv(false, obj.username, rr.access, rr.refresh)
        }

        val onBadRequest = { r: Response<Result.Tokens> ->
            val errStr = r.errorBody()?.string()
            val err = gson.fromJson(errStr, Err.Register::class.java)
            if (err.email != null && err.username != null)
                "此 Email 和 Username 皆已被註冊"
            else if (err.email != null)
                "此 Email 已被註冊"
            else
                "此 Username 已被註冊"
        }

        val onFail = {
            dialog.pos.isEnabled = true
        }

        request(this, onSucceed, onBadRequest, onFail) {
            retro.register(obj)
        }
    }

    private fun reqLogin(obj: Request.Login, dialog: AlertDialog, reme: Boolean) {
        val onSucceed = { r: Response<Result.Tokens> ->
            dialog.dismiss()
            val rr = r.body()!!
            logInEnv(reme, obj.username_or_email, rr.access, rr.refresh)
        }

        val onBadRequest = { r: Response<Result.Tokens> ->
            val errStr = r.errorBody()?.string()
            val err = gson.fromJson(errStr, Err.Login::class.java)
            when (err.non_field_errors[0]) {
                "Email does not exist." -> "Email 不存在"
                "Username does not exist." -> "Username 不存在"
                "Incorrect password." -> "密碼錯誤"
                else -> "登入失敗"
            }
        }

        val onFail = {
            dialog.pos.isEnabled = true
        }

        request(this, onSucceed, onBadRequest, onFail) {
            retro.logIn(obj)
        }
    }

    private fun reqSuggestion() {
        val atomic = AtomicBoolean(false)
        var content: String

        val dialog = dialog("AI 建議", "耐心等待6️⃣0️⃣秒", pos = "取消", neutral = " ")

        val job1 = io {
            val nums = arrayOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣")

            for (i in 60 downTo 0) {
                if (atomic.get())
                    break

                val msg = "耐心等待${nums[i / 10]}${nums[i % 10]}秒\n\n" +
                        i.toString(2).replace("0", "🌑").replace("1", "🌕")

                ui { dialog.setMessage(msg) }
                delay(1000)
            }
        }

        val onSucceed = { r: Response<Result.ChatRoot> ->
            val res = r.body()!!.response
            content = res.message.content
            atomic.set(true)
            job1.cancel()

            dialog.setTitle("${res.model} 建議")
            dialog.setMessage(content)
            dialog.pos.apply {
                text = "OK"
                dialog.setCancelJob(this)
            }
            dialog.neutral.apply {
                text = "分享"
                setOnClickListener { share(content) }
            }
            Unit
        }

        dialog.setOnDismissListener {
            job1.cancel()
        }

        request(this, onSucceed, null, null) {
            retroLong.suggest(access!!)
        }
    }

    private fun reqPredictDiabetes(
        obj: Request.Diabetes,
        dialog: AlertDialog,
        simpleItems: Array<String>,
        objItems: Array<String>
    ) {
        val onSucceed = { r: Response<Result.Diabetes> ->
            dialog.dismiss()

            val binding = DialogDiabetesOutBinding.inflate(layoutInflater)
            binding.apply {
                val isDiagnosed = r.body()!!.prediction == 1
                if (isDiagnosed) {
                    iv.setImageResource(R.drawable.exclamation_mark)
                    tv.text = "是\n\n本預測僅供參考，請務必諮詢專業醫護人員"
                } else {
                    iv.setImageResource(R.drawable.check)
                    tv.text = "否\n\n本預測僅供參考，請務必諮詢專業醫護人員"
                }

                obj.run {
                    val disease =
                        if (hypertension && heart_disease) "高血壓、心臟病"
                        else if (hypertension) "高血壓"
                        else if (heart_disease) "心臟病"
                        else "無"

                    val content =
                        "性別：${if (gender == "male") "男" else "女"}\n" +
                                "疾病史：$disease\n" +
                                "吸菸史：${simpleItems[objItems.indexOf(smoking_history)]}\n" +
                                "年齡：$age\n" +
                                "BMI：$bmi\n" +
                                "Hb1Ac值：$HbA1c_level\n" +
                                "血糖值：$blood_glucose_level\n\n" +
                                "預測結果：${if (isDiagnosed) "是" else "否"}"

                    viewDialog("預測結果", root, false, "分享").neutral.setOnClickListener {
                        share(content)
                    }
                }
            }
            Unit
        }

        request(this, onSucceed, null, null) {
            retro.predictDiabetes(access!!, obj)
        }
    }
}