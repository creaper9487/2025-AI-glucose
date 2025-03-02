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
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object Client {
    private val gson by lazy { Gson() }
    private lateinit var addr: String
    lateinit var retro: Lazy<Api>
    private lateinit var retroLong: Lazy<Api>
    fun resetRetro(addr: String) {
        Client.addr = addr
        retro = lazy { retroInit(false, addr) }
        retroLong = lazy { retroInit(true, addr) }
    }

    private fun retroInit(isLong: Boolean, addr: String): Api {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .readTimeout(if (isLong) 60 else 3, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl("http://$addr:8000/")
            .client(okHttpClient)
            .build()
            .create(Api::class.java)
    }

    fun register(accFrag: AccFrag, obj: Request.Register, dialog: AlertDialog) {
        val retroFun = suspend { retro.value.register(obj) }

        val onSucceed = { r: Response<Result.Tokens> ->
            dialog.dismiss()
            val authData = AccFrag.authDataBuilder(obj.username, r.body()!!)
            accFrag.logInEnv(authData, null)
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

    fun logIn(accFrag: AccFrag, obj: Request.Login, dialog: AlertDialog, reme: Boolean) {
        val retroFun = suspend { retro.value.logIn(obj) }

        val onSucceed = { r: Response<Result.Tokens> ->
            dialog.dismiss()
            val authData = AccFrag.authDataBuilder(obj.username_or_email, r.body()!!)
            accFrag.logInEnv(authData, reme)
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
        AccFrag.authData?.also {
            val retroFun = suspend { retro.value.getRecords(AccFrag.authData!!.access) }

            val onSucceed = { _: Response<List<Result.Records>> ->
            }

            request(chartFrag, retroFun, onSucceed, null)
        }
    }

    fun postRecord(recordFrag: RecordFrag, obj: Request.Record) {
        val retroFun = suspend { retro.value.postRecord(AccFrag.authData!!.access, obj) }

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
        val retroFun = suspend { retro.value.predictCarbohydrate(AccFrag.authData!!.access, image) }

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
            .setMessage("耐心等待6️⃣0️⃣秒")
            .setPositiveButton("取消", null)
            .setNegativeButton(" ", null)
            .setNeutralButton(" ", null)
            .show()

        accFrag.io {
            var i = 60
            while (i >= 0 && content.isEmpty()) {
                val nums = arrayOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣")
                val fruits = arrayOf("🥝", "🍇", "🍈", "🍉", "🍊", "🍌", "🍍")
                val foods = arrayOf("🍕", "🍔", "🍟", "🌭", "🥘", "🥞", "🍤")
                val a = i / 8
                val msg = "耐心等待${nums[i / 10]}${nums[i % 10]}秒\n\n" +
                        "${fruits.take(a).joinToString("")}\n${foods.take(i - a * 8).joinToString("")}\n\n" +
                        i.toString(2).replace("0", "🌑").replace("1", "🌕")

                accFrag.ui { dialog.setMessage(msg) }
                delay(1000)
                i--
            }
            accFrag.ui { dialog.setMessage(content) }
        }

        val retroFun = suspend { retroLong.value.suggest(AccFrag.authData!!.access) }

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
        val retroFun = suspend { retro.value.predictDiabetes(AccFrag.authData!!.access, obj) }

        val onSucceed = { r: Response<Result.Diabetes> ->
            accFrag.dialog("TODO: 預測結果", if (r.body()!!.prediction == 1) "是" else "否")
        }

        request(accFrag, retroFun, onSucceed, null)
    }

    private fun <T> request(
        frag: Fragment,
        retroFun: suspend () -> Response<T>,
        onSucceed: (Response<T>) -> Any?,
        onBadRequest: ((Response<T>) -> String)?
    ) {
        val setHostErrDialog = { msg: String ->
            val dialog = frag.errDialog(msg, "設定 Host")
            dialog.neutral.setOnClickListener {
                dialog.dismiss()
                (frag.requireActivity() as MainActivity).setHost()
            }
        }

        frag.io {
            try {
                var r: Response<T>? = null
                var status = 0

                for (i in 0..1) {
                    r = retroFun()
                    status = r.code()

                    if (status == 401)
                        refresh(frag)
                    else
                        break
                }

                frag.ui {
                    when (status) {
                        200, 201 -> onSucceed(r!!)
                        400 -> errDialog(
                            if (onBadRequest == null) "請求錯誤" else onBadRequest(r!!)
                        )

                        else -> errDialog("HTTP $status")
                    }
                }
            } catch (e: ConnectException) {
                frag.ui { setHostErrDialog("無法連線到$addr") }
            } catch (_: SocketTimeoutException) {
                frag.ui { setHostErrDialog("連線逾時") }
            } catch (e: Exception) {
                frag.ui { excDialog(e) }
            }
        }
    }

    fun refresh(frag: Fragment, refresh: String? = null, dialog: AlertDialog? = null, reme: Boolean? = null) {
        val obj = Request.Refresh(refresh ?: AccFrag.authData!!.refresh)
        val retroFun = suspend { retro.value.refresh(obj) }

        val onSucceed = { r: Response<Result.Refresh> ->
            AccFrag.authData = AccFrag.authDataBuilder(r.body()!!)

            if (refresh != null && frag is AccFrag) {
                dialog!!.dismiss()
                frag.logInEnv(null, reme)
            }
        }

        request(frag, retroFun, onSucceed, null)
    }
}