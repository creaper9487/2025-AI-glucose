package rl.collab.diabeat

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.delay
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import rl.collab.diabeat.frag.AccFrag
import rl.collab.diabeat.frag.ChartFrag
import rl.collab.diabeat.frag.RecordFrag
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object Client {
    var host = arrayOf("192", "168", "0", "0")

    private val gson = Gson()

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .build()
    }

    private var retrofit = lazy(::retroInit)

    private fun retroInit(): Api {
        return Retrofit.Builder()
            .baseUrl("http://${host.joinToString(".")}:8000/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(Api::class.java)
    }

    fun resetRetro() {
        retrofit = lazy(::retroInit)
    }

    fun register(accFrag: AccFrag, obj: Request.Register, dialogDismiss: () -> Unit) {
        val retroFun = suspend { retrofit.value.register(obj) }

        val onSucceed = { r: Response<Result.Tokens> ->
            dialogDismiss()
            accFrag.logInEnv(r.body()!!, obj.username, obj.password)
        }

        val onBadRequest = { r: Response<Result.Tokens> ->
            val errStr = r.errorBody()?.string()
            val err = gson.fromJson(errStr, Err.Register::class.java)

            if (err.email == null && err.username == null)
                "此 Email 和 Username 皆已被註冊"
            else if (err.email == null)
                "此 Email 已被註冊"
            else
                "此 Username 已被註冊"
        }

        request(accFrag, retroFun, onSucceed, onBadRequest)
    }

    fun logIn(accFrag: AccFrag, obj: Request.Login, dialogDismiss: () -> Unit, reme: Boolean) {
        val retroFun = suspend { retrofit.value.logIn(obj) }

        val onSucceed = { r: Response<Result.Tokens> ->
            dialogDismiss()

            if (reme)
                accFrag.accFile.writeText(obj.username_or_email)
            else {
                accFrag.accFile.delete()
                accFrag.pwFile.delete()
            }

            accFrag.logInEnv(r.body()!!, obj.username_or_email, obj.password)
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

        request(accFrag, retroFun, onSucceed, onBadRequest)
    }

    fun getRecords(chartFrag: ChartFrag) {
        AccFrag.tokens?.let {
            val retroFun = suspend { retrofit.value.getRecords(AccFrag.key) }

            val onSucceed = { _: Response<List<Result.Records>> ->
            }

            request(chartFrag, retroFun, onSucceed, null)
        }
    }

    fun postRecord(recordFrag: RecordFrag, obj: Request.Record) {
        val retroFun = suspend { retrofit.value.postRecord(AccFrag.key, obj) }

        val onSucceed = { _: Response<Result.Records> ->
            recordFrag.toast("已儲存")
            recordFrag.binding.run {
                glucoseEt.setText("")
                carbohydrateEt.setText("")
                exerciseEt.setText("")
                insulinEt.setText("")
                etGroup.clearFocus()
            }
        }

        request(recordFrag, retroFun, onSucceed, null)
    }

    fun predictCarbohydrate(recordFrag: RecordFrag, image: MultipartBody.Part) {
        val retroFun = suspend { retrofit.value.predictCarbohydrate(AccFrag.key, image) }

        val onSucceed = { r: Response<Result.Predict> ->
            recordFrag.binding.carbohydrateEt.setText(
                r.body()!!.predicted_value.roundToInt().toString()
            )
        }

        request(recordFrag, retroFun, onSucceed, null)
    }

    fun suggest(accFrag: AccFrag) {
        var content = ""

        val dialog = MaterialAlertDialogBuilder(accFrag.requireContext())
            .setCancelable(false)
            .setTitle("AI 建議")
            .setMessage("耐心等待 60s\n\n/")
            .setPositiveButton("取消", null)
            .setNegativeButton(" ", null)
            .setNeutralButton(" ", null)
            .show()

        accFrag.io {
            var i = 480
            while (i >= 0 && content == "") {
                accFrag.ui { dialog.setMessage("耐心等待 ${i / 8}s\n\n${"/-\\|"[i % 4]}") }
                delay(125)
                i--
            }
            accFrag.ui { dialog.setMessage(content) }
        }

        val retroFun = suspend {
            val customOkHttpClient = okHttpClient.newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val customRetrofit = Retrofit.Builder()
                .baseUrl("http://${host.joinToString(".")}:8000/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(customOkHttpClient)
                .build()
                .create(Api::class.java)

            customRetrofit.suggest(AccFrag.key)
        }

        val onSucceed = { r: Response<Result.ChatRoot> ->
            val res = r.body()!!.response
            content = res.message.content

            dialog.setTitle("${res.model} 建議")

            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val neutralBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            posBtn.text = "OK"

            negBtn.text = "複製"
            negBtn.setOnClickListener {
                val clip = ClipData.newPlainText("", content)
                val clipboard = accFrag.requireContext().getSystemService(Service.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(clip)
                accFrag.toast("已複製✅")
            }

            neutralBtn.text = "分享"
            neutralBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_TEXT, content)
                intent.type = "text/plain"

                accFrag.startActivity(Intent.createChooser(intent, null))
            }
        }

        request(accFrag, retroFun, onSucceed, null)
    }

    fun predictDiabetes(accFrag: AccFrag, obj: Request.Diabetes) {
        val retroFun = suspend { retrofit.value.predictDiabetes(AccFrag.key, obj) }

        val onSucceed = { r: Response<Result.Diabetes> ->
            accFrag.dialog("TODO: 預測結果", if (r.body()!!.prediction == 1) "是" else "否")
        }

        request(accFrag, retroFun, onSucceed, null)
    }

    private fun <T> request(
        frag: Fragment,
        retroFun: suspend () -> Response<T>,
        onSucceed: (Response<T>) -> Unit,
        onBadRequest: ((Response<T>) -> String)?
    ) {
        frag.io {
            try {
                lateinit var r: Response<T>
                var status = 0

                for (i in 0..1) {
                    r = retroFun()
                    status = r.code()

                    if (status == 401)
                        refresh()
                    else
                        break
                }

                frag.ui {
                    when (status) {
                        200, 201 -> onSucceed(r)
                        400 -> errDialog(
                            if (onBadRequest == null) "請求錯誤" else onBadRequest(r)
                        )

                        else -> errDialog("HTTP $status")
                    }
                }
            } catch (_: SocketTimeoutException) {
                frag.ui { errDialog("連線逾時") }
            } catch (e: Exception) {
                frag.ui { errDialog("${e::class.java.name}\n\n${e.message}") }
            }
        }
    }

    private suspend fun refresh() {
        val obj = Request.Refresh(AccFrag.tokens!!.refresh)
        val r = retrofit.value.refresh(obj)
        AccFrag.tokens = r.body()
    }
}