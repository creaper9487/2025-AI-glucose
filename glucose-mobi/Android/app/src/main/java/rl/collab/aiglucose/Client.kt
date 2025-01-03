package rl.collab.aiglucose

import android.app.AlertDialog
import android.content.Context
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import rl.collab.aiglucose.frag.AccFrag
import kotlin.reflect.KSuspendFunction1

object Client {
    var host = "192.168.163.196"

    private val retrofit: Api by lazy {
        Retrofit.Builder()
            .baseUrl("http://$host:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
    }

    fun register(accFrag: AccFrag, request: Request.Register, dialog: AlertDialog) {
        val onOk = { _: Response<Result.Token> ->
            dialog.dismiss()
            accFrag.logIn()

            val fileOutput = accFrag.requireContext().openFileOutput(AccFrag.ACC_JSON, Context.MODE_PRIVATE)
            fileOutput.bufferedWriter().use { bw ->
                val logInReq = Request.LogIn(request.username, request.password)
                bw.write(Gson().toJson(logInReq))
            }
        }

        val onBadRequest = { response: Response<Result.Token> ->
            val errJson = response.errorBody()?.string()
            val errRes = Gson().fromJson(errJson, Err.Register::class.java)

            val errMsgs = mutableListOf<String>()
            errRes.email?.let { errMsgs.add(accFrag.getString(R.string.email_registered)) }
            errRes.username?.let { errMsgs.add(accFrag.getString(R.string.username_registered)) }

            errMsgs.joinToString("\n")
        }

        request(accFrag, retrofit::register, request, 201, onOk, onBadRequest)
    }

    fun logIn(accFrag: AccFrag, request: Request.LogIn, dialog: AlertDialog) {
        val onOk = { _: Response<Result.Token> ->
            dialog.dismiss()
            accFrag.logIn()

            val fileOutput = accFrag.requireContext().openFileOutput(AccFrag.ACC_JSON, Context.MODE_PRIVATE)
            fileOutput.bufferedWriter().use { bw ->
                bw.write(Gson().toJson(request))
            }
        }

        val onBadRequest = { _: Response<Result.Token> ->
            accFrag.getString(R.string.log_in_fail)
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
            } catch (e: Exception) {
                frag.ui { errDialog(e::class.java.simpleName) }
            }
        }
    }
}