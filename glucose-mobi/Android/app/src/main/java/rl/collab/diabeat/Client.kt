package rl.collab.diabeat

import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Client {
    lateinit var addr: String
    lateinit var retro: Lazy<Api>
    lateinit var retroLong: Lazy<Api>
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
}