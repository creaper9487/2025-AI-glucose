package rl.collab.diabeat

import androidx.fragment.app.Fragment
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://${host.joinToString(".")}:8000/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(Api::class.java)
    }

    fun register(accFrag: AccFrag, obj: Request.Register, dialogDismiss: () -> Unit) {
        val retroFun = suspend { retrofit.register(obj) }

        val onSucceed = { r: Response<Result.Token> ->
            dialogDismiss()
            accFrag.logInEnv(r.body()!!, obj.username, obj.password)
        }

        val onBadRequest = { r: Response<Result.Token> ->
            val errStr = r.errorBody()?.string()
            val err = Gson().fromJson(errStr, Err.Register::class.java)

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
        val retroFun = suspend { retrofit.logIn(obj) }

        val onSucceed = { r: Response<Result.Token> ->
            dialogDismiss()

            if (reme)
                accFrag.accFile.writeText(obj.usernameOrEmail)
            else {
                accFrag.accFile.delete()
                accFrag.pwFile.delete()
            }

            accFrag.logInEnv(r.body()!!, obj.usernameOrEmail, obj.password)
        }

        val onBadRequest = { r: Response<Result.Token> ->
            val errStr = r.errorBody()?.string()
            val err = Gson().fromJson(errStr, Err.Login::class.java)

            when (err.nonFieldErrors[0]) {
                "Email does not exist." -> "Email 不存在"
                "Username does not exist." -> "Username 不存在"
                "Incorrect password." -> "密碼錯誤"
                else -> "登入失敗"
            }
        }

        request(accFrag, retroFun, onSucceed, onBadRequest)
    }

    fun getRecords(chartFrag: ChartFrag) {
        AccFrag.token?.let {
            val retroFun = suspend { retrofit.getRecords(AccFrag.token!!.access.bearer) }

            val onSucceed = { r: Response<List<Result.Records>> ->
                chartFrag.data.clear()
                chartFrag.data.addAll(r.body()!!)
                chartFrag.table.adapter!!.notifyDataSetChanged()
            }

            request(chartFrag, retroFun, onSucceed, null)
        }
    }

    fun postRecord(recordFrag: RecordFrag, obj: Request.Record) {
        val retroFun = suspend { retrofit.postRecord(AccFrag.token!!.access.bearer, obj) }

        val onSucceed = { _: Response<Result.Records> ->
            recordFrag.shortToast("已儲存")
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

    fun predict(recordFrag: RecordFrag, image: MultipartBody.Part) {
        val retroFun = suspend { retrofit.predict(AccFrag.token!!.access.bearer, image) }

        val onSucceed = { r: Response<Result.Predict> ->
            recordFrag.binding.carbohydrateEt.setText(
                r.body()!!.predictedValue.roundToInt().toString()
            )
        }

        request(recordFrag, retroFun, onSucceed, null)
    }

    fun chat(chartFrag: ChartFrag) {
        val retroFun = suspend { retrofit.chat(AccFrag.token!!.access.bearer) }

        val onSucceed = { r: Response<Result.ChatRoot> ->
            chartFrag.dialog("AI Assistant", r.body()!!.response.message.content)
            chartFrag.binding.chatBtn.isEnabled = true
        }

        request(chartFrag, retroFun, onSucceed, null)
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

                    if (status == 401) {
                        val refreshResponse = retrofit.refresh(Request.Refresh(AccFrag.token!!.refresh))
                        AccFrag.token = refreshResponse.body()
                    } else
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
                frag.ui { dialog(e::class.java.name, e.message.toString()) }
            }
        }
    }
}