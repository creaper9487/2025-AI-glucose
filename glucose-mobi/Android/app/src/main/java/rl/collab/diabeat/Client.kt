package rl.collab.diabeat

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import rl.collab.diabeat.frag.AccFrag
import rl.collab.diabeat.frag.ChartFrag
import rl.collab.diabeat.frag.RecordFrag
import rl.collab.diabeat.frag.TableAdapter
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

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

    fun register(accFrag: AccFrag, obj: Request.Register, dialogDismiss: () -> Unit) {
        val retroFun = suspend { retrofit.register(obj) }

        val onOk = { r: Response<Result.Token> ->
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

        request(accFrag, retroFun, 201, onOk, onBadRequest)
    }

    fun logIn(accFrag: AccFrag, obj: Request.Login, dialogDismiss: () -> Unit, reme: Boolean) {
        val retroFun = suspend { retrofit.logIn(obj) }

        val onOk = { r: Response<Result.Token> ->
            dialogDismiss()

            if (reme)
                accFrag.accFile.writeText(obj.username_or_email)
            else {
                accFrag.accFile.delete()
                accFrag.pwFile.delete()
            }

            accFrag.logInEnv(r.body()!!, obj.username_or_email, obj.password)
        }

        val onBadRequest = { r: Response<Result.Token> ->
            val errStr = r.errorBody()?.string()
            val err = Gson().fromJson(errStr, Err.Login::class.java)

            when (err.non_field_errors[0]) {
                "Email does not exist." -> "Email 不存在"
                "Username does not exist." -> "Username 不存在"
                "Incorrect password." -> "密碼錯誤"
                else -> "登入失敗"
            }
        }

        request(accFrag, retroFun, 200, onOk, onBadRequest)
    }

    fun getRecords(chartFrag: ChartFrag) {
        AccFrag.token?.let {
            val retroFun = suspend { retrofit.getRecords(AccFrag.token!!.access.bearer) }

            val onOk = { r: Response<List<Result.Records>> ->
                chartFrag.data.clear()
                chartFrag.data.addAll(r.body()!!)
                chartFrag.table.adapter!!.notifyDataSetChanged()
            }

            val onBadRequest = { _: Response<List<Result.Records>> ->
                "nig"
            }

            request(chartFrag, retroFun, 200, onOk, onBadRequest)
        }
    }

    fun postRecord(recordFrag: RecordFrag, obj: Request.Record) {
        val retroFun = suspend { retrofit.postRecord(AccFrag.token!!.access.bearer, obj) }

        val onOk = { _: Response<Result.Records> ->
            recordFrag.shortToast("已儲存")
            recordFrag.binding.run {
                glucoseEt.setText("")
                carbohydrateEt.setText("")
                exerciseEt.setText("")
                insulinEt.setText("")
                etGroup.clearFocus()
            }
        }

        val onBadRequest = { _: Response<Result.Records> ->
            "nah"
        }

        request(recordFrag, retroFun, 201, onOk, onBadRequest)
    }

    fun predict(recordFrag: RecordFrag, image: MultipartBody.Part) {
        val retroFun = suspend { retrofit.predict(AccFrag.token!!.access.bearer, image) }

        val onOk = { r: Response<Result.Predict> ->
            recordFrag.binding.carbohydrateEt.setText("${r.body()!!.predicted_value.roundToInt()}")
        }

        val onBadRequest = { _: Response<Result.Predict> ->
            "bad request"
        }

        request(recordFrag, retroFun, 200, onOk, onBadRequest)
    }

    private fun <T> request(
        frag: Fragment,
        retroFun: suspend () -> Response<T>,
        okCode: Int,
        onOk: (Response<T>) -> Unit,
        onBadRequest: (Response<T>) -> String
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
                        okCode -> onOk(r)
                        400 -> errDialog(onBadRequest(r))
                        else -> errDialog("HTTP $status")
                    }
                }
            } catch (_: SocketTimeoutException) {
                frag.ui { errDialog("連線逾時") }
            } catch (e: Exception) {
                frag.ui { errDialog("${e.message}", e::class.java.name) }
            }
        }
    }
}