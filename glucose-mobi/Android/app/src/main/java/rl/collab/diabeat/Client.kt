package rl.collab.diabeat

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import rl.collab.diabeat.frag.AccFrag
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

object Client {
    private lateinit var addr: String
    private lateinit var retro: Lazy<Api>
    private lateinit var retroLong: Lazy<Api>
    fun resetRetro(addr: String) {
        fun retroInit(isLong: Boolean): Api {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(if (isLong) 60 else 3, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .baseUrl("http://${Client.addr}:8000/")
                .client(okHttpClient)
                .build()
                .create(Api::class.java)
        }

        Client.addr = addr
        retro = lazy { retroInit(false) }
        retroLong = lazy { retroInit(true) }
    }

    fun Fragment.refresh(refresh: String? = null, dialog: AlertDialog? = null, reme: Boolean? = null) {
        val onSucceed = { r: Response<Result.Refresh> ->
            AccFrag.access = "Bearer ${r.body()}"
            refresh?.also {
                dialog!!.dismiss()

                val rr = r.body()!!
                (this as AccFrag).logInEnv(reme, rr.username, rr.access, rr.refresh)
            }
            Unit
        }

        request(onSucceed, null, null, false) {
            val obj = Request.Refresh(refresh ?: AccFrag.refresh!!)
            refresh(obj)
        }
    }

    fun <T> Fragment.request(
        onSucceed: (Response<T>) -> Unit,
        onBadRequest: ((Response<T>) -> String)?,
        onFail: (() -> Unit)?,
        isLong: Boolean,
        retroFun: suspend Api.() -> Response<T>
    ) =
        viewLifecycleScope.launch {
            try {
                var r: Response<T>? = null
                var status = 0

                withContext(Dispatchers.IO) {
                    for (i in 0..1) {

                        r = (if (isLong) retroLong else retro).value.retroFun()
                        status = r!!.code()
                        if (status == 401)
                            refresh()
                        else
                            break
                    }
                }

                when (status) {
                    200, 201 -> onSucceed(r!!)
                    400 -> errDialog(onBadRequest?.invoke(r!!) ?: "請求錯誤")
                    401 -> {
                        errDialog("無密碼登入憑證過期，請重新登入")
                        AccFrag.remePref.syncEdit { clear() }
                    }

                    else -> errDialog("HTTP $status")
                }

                if (status != 200 && status != 201)
                    onFail?.invoke()

            } catch (e: Exception) {
                fun setHostErrDialog(msg: String) {
                    val dialog = errDialog(msg, "設定 Host")
                    dialog.neutral.setOnClickListener {
                        dialog.dismiss()
                        (requireActivity() as MainActivity).setHost()
                    }
                }

                when (e) {
                    is CancellationException -> {}
                    is ConnectException -> setHostErrDialog("無法連線到$addr")
                    is SocketTimeoutException -> setHostErrDialog("連線逾時")
                    else -> exceptionDialog(e)
                }
                onFail?.invoke()
            }
        }
}