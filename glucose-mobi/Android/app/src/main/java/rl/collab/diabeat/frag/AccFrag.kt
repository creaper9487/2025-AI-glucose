package rl.collab.diabeat.frag

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.core.widget.doAfterTextChanged
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.gson.Gson
import io.noties.markwon.Markwon
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import retrofit2.Response
import rl.collab.diabeat.Err
import rl.collab.diabeat.R
import rl.collab.diabeat.Request
import rl.collab.diabeat.Result
import rl.collab.diabeat.databinding.DialogDiabetesInputBinding
import rl.collab.diabeat.databinding.DialogDiabetesOutputBinding
import rl.collab.diabeat.databinding.DialogLoginBinding
import rl.collab.diabeat.databinding.DialogRegisterBinding
import rl.collab.diabeat.databinding.FragAccBinding
import rl.collab.diabeat.ntr
import rl.collab.diabeat.pos
import rl.collab.diabeat.str
import rl.collab.diabeat.syncEdit
import java.util.concurrent.atomic.AtomicBoolean

class AccFrag : MyFrag<FragAccBinding>() {
    private val notLoginView get() = binding.profileLy.visibility != View.VISIBLE

    private val remeAcc get() = vm.remePref.getString("acc", null)
    private val remeRefresh get() = vm.remePref.getString("refresh", null)
    private val remeCanBio get() = remeRefresh != null
    private val remeStrong get() = vm.remePref.getBoolean("strong", false)

    private val canBio
        get() = BiometricManager.from(con).canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
    private val canStrongBio
        get() = BiometricManager.from(con).canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) =
        FragAccBinding.inflate(inflater, container, false)

    override fun FragAccBinding.setView() {
        googleSignInBtn.setOnClickListener { reqGoogleSignIn() }
        registerBtn.setOnClickListener { reqRegistration() }
        loginBtn.setOnClickListener { reqLogin() }
        aboutUsBtn.setOnClickListener {
            val uri = Uri.parse("https://github.com/creaper9487/2025-AI-glucose")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        suggestBtn.setOnClickListener { reqSuggestion() }
        predictDiabetesBtn.setOnClickListener { reqPredictDiabetes() }
        logOutBtn.setOnClickListener { logOutEnv() }
        bioLoginSw.setOnCheckedChangeListener { _, isChecked ->
            if (notLoginView)
                return@setOnCheckedChangeListener

            vm.remePref.syncEdit {
                if (isChecked) {
                    strongBioSw.isEnabled = canStrongBio
                    putString("acc", vm.acc)
                    putString("refresh", vm.refresh)
                } else {
                    strongBioSw.isEnabled = false
                    clear()
                }
                strongBioSw.isChecked = false
                strongBioSw.jumpDrawablesToCurrentState()
            }
        }
        strongBioSw.setOnCheckedChangeListener { _, isChecked ->
            if (notLoginView)
                return@setOnCheckedChangeListener

            vm.remePref.syncEdit {
                if (isChecked)
                    putBoolean("strong", true)
                else
                    remove("strong")
            }
        }
        vm.acc?.also { logInEnv(null) } ?: logOutEnv()
    }

    fun logInEnv(reme: Boolean?, pAcc: String? = null, pAccessRaw: String? = null, pRefresh: String? = null) {
        pAcc?.also {
            vm.acc = pAcc
            vm.access = "Bearer $pAccessRaw"
            vm.refresh = pRefresh
        }

        reme?.also {
            vm.remePref.syncEdit {
                if (reme)
                    putString("acc", vm.acc)
                else
                    clear()
            }
        }

        binding.apply {
            accLy.visibility = View.INVISIBLE
            profileLy.visibility = View.VISIBLE
            profileTv.text = "Hi, ${vm.acc}"

            if (canStrongBio) {
                strongBioSw.isEnabled = true
                strongBioSw.isChecked = remeStrong

                bioLoginSw.isEnabled = true
                bioLoginSw.isChecked = remeCanBio
            } else if (canBio) {
                strongBioSw.isEnabled = false
                strongBioSw.isChecked = false

                bioLoginSw.isEnabled = true
                bioLoginSw.isChecked = remeCanBio
            } else {
                strongBioSw.isEnabled = false
                strongBioSw.isChecked = false

                bioLoginSw.isEnabled = false
                bioLoginSw.isChecked = false
            }
            strongBioSw.jumpDrawablesToCurrentState()
            bioLoginSw.jumpDrawablesToCurrentState()
        }
    }

    private fun bioLogIn(dialog: AlertDialog, reme: Boolean) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("生物辨識登入")
            .setNegativeButtonText("取消")
            .setAllowedAuthenticators(if (remeStrong) BIOMETRIC_STRONG else BIOMETRIC_WEAK)
            .build()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                refresh(remeRefresh, dialog, reme)
            }
        }

        BiometricPrompt(this, callback).authenticate(promptInfo)
    }

    private fun logOutEnv() {
        vm.acc = null
        vm.access = null
        vm.refresh = null
        binding.profileLy.visibility = View.INVISIBLE
        binding.accLy.visibility = View.VISIBLE
    }

    //
    //
    // Request

    private fun reqGoogleSignIn() {
        val opt = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.token))
            .build()

        val credentialObj = GetCredentialRequest.Builder()
            .addCredentialOption(opt)
            .build()

        ui {
            try {
                val credential = CredentialManager.create(requireContext())
                    .getCredential(requireContext(), credentialObj)
                    .credential

                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    toast("無法獲取有效憑證")
                    return@ui
                }

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                // val email = googleIdTokenCredential.id
                val idToken = googleIdTokenCredential.idToken
                // val name = googleIdTokenCredential.displayName

                val obj = Request.GoogleSignIn(idToken)
                val onSucceed = { _: Response<Unit> ->
                }
                request(onSucceed, null, null, false) { googleSignIn(obj) }

            } catch (_: GetCredentialCancellationException) {
            } catch (e: NoCredentialException) {
                errDialog("沒有找到可用的憑證")
            } catch (e: Exception) {
                exceptionDialog(e)
            }
        }
    }

    private fun reqRegistration() {
        val binding = DialogRegisterBinding.inflate(layoutInflater)
        binding.apply {
            val dialog = reqDialog("註冊", view = root)
            val pos = dialog.pos

            pos.isEnabled = false
            pos.setOnClickListener {
                pos.isEnabled = false

                val obj = Request.Register(emailEt.str, usernameEt.str, pwEt.str)
                val onSucceed = { r: Response<Result.Tokens> ->
                    dialog.dismiss()
                    val rr = r.body()!!
                    logInEnv(false, obj.username, rr.access, rr.refresh)
                }
                val onBadRequest = { r: Response<Result.Tokens> ->
                    val errStr = r.errorBody()?.string()
                    val err = Gson().fromJson(errStr, Err.Register::class.java)
                    if (err.email != null && err.username != null)
                        "此 Email 和 Username 皆已被註冊"
                    else if (err.email != null)
                        "此 Email 已被註冊"
                    else
                        "此 Username 已被註冊"
                }
                val onFail = { dialog.pos.isEnabled = true }
                request(onSucceed, onBadRequest, onFail, false) { register(obj) }
            }
            val watcher = { _: Editable? ->
                pos.isEnabled = Patterns.EMAIL_ADDRESS.matcher(emailEt.str).matches() &&
                        usernameEt.str.isNotEmpty() &&
                        pwEt.str.isNotEmpty()
            }
            emailEt.doAfterTextChanged(watcher)
            usernameEt.doAfterTextChanged(watcher)
            pwEt.doAfterTextChanged(watcher)
        }
    }

    private fun reqLogin() {
        val binding = DialogLoginBinding.inflate(layoutInflater)
        binding.apply {
            remeAcc?.also {
                accEt.setText(it)
                remeCb.isChecked = true
            }

            val dialog = dialog("登入", view = root, neutral = "生物辨識")
            val pos = dialog.pos
            val ntr = dialog.ntr

            pos.isEnabled = false
            pos.setOnClickListener {
                pos.isEnabled = false
                ntr.isEnabled = false

                val obj = Request.Login(accEt.str.trim(), pwEt.str)
                val onSucceed = { r: Response<Result.Tokens> ->
                    dialog.dismiss()
                    val rr = r.body()!!
                    logInEnv(remeCb.isChecked, obj.username_or_email, rr.access, rr.refresh)
                }
                val onBadRequest = { r: Response<Result.Tokens> ->
                    val errStr = r.errorBody()?.string()
                    val err = Gson().fromJson(errStr, Err.Login::class.java)
                    when (err.non_field_errors[0]) {
                        "Email does not exist." -> "Email 不存在"
                        "Username does not exist." -> "Username 不存在"
                        "Incorrect password." -> "密碼錯誤"
                        else -> "登入失敗"
                    }
                }
                val onFail = {
                    pos.isEnabled = true
                    ntr.isEnabled = true
                }

                request(onSucceed, onBadRequest, onFail, false) { logIn(obj) }
            }
            val watcher = { _: Editable? ->
                pos.isEnabled = accEt.str.isNotEmpty() && pwEt.str.isNotEmpty()
            }
            accEt.doAfterTextChanged(watcher)
            pwEt.doAfterTextChanged(watcher)
            pwEt.setOnEditorActionListener { _, _, _ ->
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(pwEt.windowToken, 0)
                pos.callOnClick()
            }

            if (remeCanBio) {
                ntr.setOnClickListener { _ ->
                    bioLogIn(dialog, remeCb.isChecked)
                }
                ntr.callOnClick()
            } else
                ntr.isEnabled = false
        }
    }

    private fun reqSuggestion() {
        val atomic = AtomicBoolean(false)
        var res: Result.Chat? = null
        val onSucceed = { r: Response<Result.ChatRoot> ->
            res = r.body()!!.response
            atomic.set(true)
        }
        request(onSucceed, null, null, true) { suggest(vm.access!!) }

        val dialog = reqDialog("AI 建議", "耐心等待6️⃣0️⃣秒", pos = "取消", neg = null, neutral = " ")
        ui {
            val nums = arrayOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣")

            for (i in 60 downTo 0) {
                if (atomic.get())
                    break

                val msg = "耐心等待${nums[i / 10]}${nums[i % 10]}秒\n\n" +
                        i.toString(2).replace("0", "🌑").replace("1", "🌕")

                dialog.setMessage(msg)
                delay(1000)
            }
            if (!atomic.get())
                return@ui

            val content = res!!.message.content
            val spannedString = Markwon.create(requireContext()).toMarkdown(content)

            dialog.setTitle("${res!!.model} 建議")
            dialog.setMessage(spannedString)
            dialog.pos.text = "OK"
            dialog.ntr.apply {
                text = "分享"
                setOnClickListener { share(content) }
            }
        }
    }

    private fun reqPredictDiabetes() {
        val b1 = DialogDiabetesInputBinding.inflate(layoutInflater)

        val ets = arrayOf(b1.smokingHistoryAc, b1.ageEt, b1.bmiEt, b1.hb1acEt, b1.glucoseEt)
        val simpleItems = arrayOf("從不吸菸", "曾經吸菸", "目前沒有吸菸", "目前有吸菸")
        val objItems = arrayOf("never", "former", "not current", "current")
        b1.smokingHistoryAc.setSimpleItems(simpleItems)

        val dialog = reqDialog("預測是否有糖尿病", view = b1.root)
        val pos = dialog.pos
        pos.isEnabled = false
        pos.setOnClickListener {
            val obj = Request.Diabetes(
                if (b1.maleRb.isChecked) "male" else "female",
                b1.ageEt.str.toInt(),
                b1.hypertensionCb.isChecked,
                b1.heartDiseaseCb.isChecked,
                objItems[simpleItems.indexOf(b1.smokingHistoryAc.str)],
                b1.bmiEt.str.toDouble(),
                b1.hb1acEt.str.toDouble(),
                b1.glucoseEt.str.toInt()
            )

            val onSucceed = { r: Response<Result.Diabetes> ->
                dialog.dismiss()

                val b2 = DialogDiabetesOutputBinding.inflate(layoutInflater)
                val isDiagnosed = r.body()!!.prediction == 1
                if (isDiagnosed) {
                    b2.iv.setImageResource(R.drawable.exclamation_mark)
                    b2.tv.text = "是\n\n本預測僅供參考，請務必諮詢專業醫護人員"
                } else {
                    b2.iv.setImageResource(R.drawable.check)
                    b2.tv.text = "否\n\n本預測僅供參考，請務必諮詢專業醫護人員"
                }

                val disease =
                    if (obj.hypertension && obj.heart_disease) "高血壓、心臟病"
                    else if (obj.hypertension) "高血壓"
                    else if (obj.heart_disease) "心臟病"
                    else "無"

                val content = "性別：${if (obj.gender == "male") "男" else "女"}\n" +
                        "疾病史：$disease\n" +
                        "吸菸史：${simpleItems[objItems.indexOf(obj.smoking_history)]}\n" +
                        "年齡：${obj.age}\n" +
                        "BMI：${obj.bmi}\n" +
                        "Hb1Ac值：${obj.HbA1c_level}\n" +
                        "血糖值：${obj.blood_glucose_level}\n\n" +
                        "預測結果：${if (isDiagnosed) "是" else "否"}"

                dialog("預測結果", view = b2.root, neg = null, neutral = "分享").ntr.setOnClickListener {
                    share(content)
                }
            }

            request(onSucceed, null, null, false) { predictDiabetes(vm.access!!, obj) }
        }
        val watcher = {
            pos.isEnabled = b1.genderRg.checkedRadioButtonId != -1 && ets.all { it.str.isNotEmpty() }
        }
        b1.genderRg.setOnCheckedChangeListener { _, _ -> watcher() }
        for (et in ets)
            et.doAfterTextChanged { watcher() }
    }

    //
    //
    // Helper

    private fun reqDialog(
        title: String,
        msg: String? = null,
        view: View? = null,
        pos: String? = "OK",
        neg: String? = "取消",
        neutral: String? = null,
    ): AlertDialog {
        val dialog = dialog(title, msg, view, pos, neg, neutral)
        dialog.setOnDismissListener {
            viewLifecycleScope.coroutineContext.cancelChildren()
        }
        return dialog
    }

    private fun share(content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))
    }
}