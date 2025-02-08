package rl.collab.diabeat

import androidx.fragment.app.Fragment
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import rl.collab.diabeat.frag.AccFrag
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.reflect.KSuspendFunction1

object Client {
    var host = arrayOf("192", "168", "0", "0")

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(1000, TimeUnit.MILLISECONDS)
            .readTimeout(1000, TimeUnit.MILLISECONDS)
            .writeTimeout(1000, TimeUnit.MILLISECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://${host.joinToString(".")}:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(Api::class.java)
    }

    fun register(accFrag: AccFrag, request: Request.Register, dialogDismiss: () -> Unit) {
        val onOk = { _: Response<Result.Token> ->
            dialogDismiss()
            accFrag.logInEnv(request.username, request.password)
        }

        val onBadRequest = { response: Response<Result.Token> ->
            val errJson = response.errorBody()?.string()
            val errRes = Gson().fromJson(errJson, Err.Register::class.java)

            if (errRes.email == null && errRes.username == null)
                "此 Email 和 Username 皆已被註冊"
            else if (errRes.email == null)
                "此 Email 已被註冊"
            else
                "此 Username 已被註冊"
        }

        request(accFrag, retrofit::register, request, 201, onOk, onBadRequest)
    }

    fun logIn(accFrag: AccFrag, request: Request.LogIn, dialogDismiss: () -> Unit, reme: Boolean) {
        val onOk = { _: Response<Result.Token> ->
            dialogDismiss()

            if (reme)
                accFrag.accFile.writeText(request.username_or_email)
            else {
                accFrag.accFile.delete()
                accFrag.pwFile.delete()
            }

            accFrag.logInEnv(request.username_or_email, request.password)
        }

        val onBadRequest = { response: Response<Result.Token> ->
            val errJson = response.errorBody()?.string()
            val errRes = Gson().fromJson(errJson, Err.LogIn::class.java)

            when (errRes.non_field_errors[0]) {
                "Email does not exist." -> "Email 不存在"
                "Username does not exist." -> "Username 不存在"
                "Incorrect password." -> "密碼錯誤"
                else -> "登入失敗"
            }
        }

        request(accFrag, retrofit::logIn, request, 200, onOk, onBadRequest)
    }

    // onBadRequest 也會 errDialog
    private fun <A, B> request(
        frag: Fragment,
        retroAction: KSuspendFunction1<A, Response<B>>,
        request: A,
        successCode: Int,
        onSucceed: (Response<B>) -> Unit,
        onBadRequest: (Response<B>) -> String
    ) {
        frag.io {
            try {
                val response = retroAction(request)

                frag.ui {
                    when (val status = response.code()) {
                        successCode -> onSucceed(response)
                        400 -> errDialog(onBadRequest(response))
                        else -> errDialog("HTTP $status")
                    }
                }
            } catch (_: SocketTimeoutException) {
                frag.ui { errDialog("連線逾時") }
            } catch (e: Exception) {
                frag.ui { errDialog(e::class.java.simpleName) }
            }
        }
    }
}