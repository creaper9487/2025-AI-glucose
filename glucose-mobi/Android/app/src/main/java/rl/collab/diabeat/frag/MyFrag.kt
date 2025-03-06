package rl.collab.diabeat.frag

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import rl.collab.diabeat.Api
import rl.collab.diabeat.Client.addr
import rl.collab.diabeat.Client.retro
import rl.collab.diabeat.Client.retroLong
import rl.collab.diabeat.MainActivity
import rl.collab.diabeat.Request
import rl.collab.diabeat.Result
import rl.collab.diabeat.dialog
import rl.collab.diabeat.ntr
import rl.collab.diabeat.pos
import rl.collab.diabeat.syncEdit
import rl.collab.diabeat.toast
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.CancellationException

typealias Binder<V> = (LayoutInflater, ViewGroup?, Boolean) -> V

abstract class MyFrag<V : ViewBinding> : Fragment() {
    private var _binding: V? = null
    protected val binding get() = _binding!!

    protected val con get() = requireContext()
    protected val appCon: Context get() = con.applicationContext!!
    private val act get() = requireActivity()
    private val main get() = act as MainActivity

    protected val vm by activityViewModels<MyViewModel>()
    protected val viewLifecycleScope get() = viewLifecycleOwner.lifecycleScope
    protected fun ui(block: suspend CoroutineScope.() -> Unit) =
        viewLifecycleScope.launch(block = block)

    private suspend fun io(block: suspend CoroutineScope.() -> Unit) =
        withContext(Dispatchers.IO) { block() }

    protected abstract fun binder(): Binder<V>

    protected abstract fun V.setView()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = binder()(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun toast(msg: String) = act.toast(msg)

    protected fun dialog(
        title: String,
        msg: String? = null,
        view: View? = null,
        pos: String? = "OK",
        neg: String? = "取消",
        neutral: String? = null,
        cancelable: Boolean = false
    ) = act.dialog(title, msg, view, pos, neg, neutral, cancelable)

    protected fun errDialog(msg: String, neutral: String? = null) =
        dialog("錯誤", msg, neg = null, neutral = neutral)

    protected fun exceptionDialog(e: Exception) {
        val z = e::class.java
        errDialog("${z.`package`?.name}\n${z.simpleName}\n\n${e.localizedMessage}")
    }

    protected fun refresh(refresh: String? = null, dialog: AlertDialog? = null, reme: Boolean? = null) {
        val onSucceed = { r: Response<Result.Refresh> ->
            vm.access = "Bearer ${r.body()}"
            refresh?.also {
                dialog!!.dismiss()

                val rr = r.body()!!
                (this as AccFrag).logInEnv(reme, rr.username, rr.access, rr.refresh)
            }
            Unit
        }

        request(onSucceed, null, null, false) {
            val obj = Request.Refresh(refresh ?: vm.refresh!!)
            refresh(obj)
        }
    }

    protected fun <T> request(
        onSucceed: (Response<T>) -> Unit,
        onBadRequest: ((Response<T>) -> String)?,
        onFail: (() -> Unit)?,
        isLong: Boolean,
        retroFun: suspend Api.() -> Response<T>
    ) =
        ui {
            try {
                var r: Response<T>? = null
                var status = 0

                io {
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
                        vm.remePref.syncEdit { clear() }
                        errDialog("無密碼登入憑證過期，請重新登入").pos.setOnClickListener {
                            main.finishAffinity()
                        }
                    }

                    else -> errDialog("HTTP $status")
                }

                if (status != 200 && status != 201)
                    onFail?.invoke()

            } catch (e: Exception) {
                fun setHostErrDialog(msg: String) {
                    val dialog = errDialog(msg, "設定 Host")
                    dialog.ntr.setOnClickListener {
                        dialog.dismiss()
                        main.setHost()
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