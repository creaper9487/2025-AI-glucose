package rl.collab.diabeat

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import rl.collab.diabeat.frag.AccFrag
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object Client {
    private var job: Job? = null
    private lateinit var addr: String
    private lateinit var _retro: Lazy<Api>
    private lateinit var _retroLong: Lazy<Api>

    val gson by lazy { Gson() }
    val retro get() = _retro.value
    val retroLong get() = _retroLong.value

    fun cancelJob() = job?.cancel()

    fun resetRetro(addr: String) {
        fun retroInit(isLong: Boolean): Api {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .readTimeout(if (isLong) 60 else 1, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl("http://$addr:8000/")
                .client(okHttpClient)
                .build()
                .create(Api::class.java)
        }

        Client.addr = addr
        _retro = lazy { retroInit(false) }
        _retroLong = lazy { retroInit(true) }
    }

    fun refresh(frag: Fragment, refresh: String? = null, dialog: AlertDialog? = null, reme: Boolean? = null) {

        val onSucceed = { r: Response<Result.Refresh> ->
            AccFrag.access = "Bearer ${r.body()}"
            refresh?.also {
                dialog!!.dismiss()

                val rr = r.body()!!
                (frag as AccFrag).logInEnv(reme, rr.username, rr.access, rr.refresh)
            }
            Unit
        }

        request(frag, onSucceed, null, null) {
            val obj = Request.Refresh(refresh ?: AccFrag.refresh!!)
            retro.refresh(obj)
        }
    }

    fun <T> request(
        frag: Fragment,
        onSucceed: (Response<T>) -> Unit,
        onBadRequest: ((Response<T>) -> String)?,
        onFail: (() -> Unit)?,
        retroFun: suspend () -> Response<T>
    ) {
        cancelJob()
        job = frag.io {
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
                        400 -> errDialog(onBadRequest?.invoke(r!!) ?: "請求錯誤")
                        401 -> {
                            errDialog("無密碼登入憑證過期，請重新登入")
                            AccFrag.remePref.syncEdit { clear() }
                        }

                        else -> errDialog("HTTP $status")
                    }

                    if (status != 200 && status != 201)
                        onFail?.invoke()
                }
            } catch (e: Exception) {
                frag.ui {
                    when (e) {
                        is ConnectException -> setHostErrDialog("無法連線到$addr")
                        is SocketTimeoutException -> setHostErrDialog("連線逾時")
                        else -> exceptionDialog(e)
                    }
                    onFail?.invoke()
                }
            }
        }
    }
}