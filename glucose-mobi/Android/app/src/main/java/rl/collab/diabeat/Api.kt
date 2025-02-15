package rl.collab.diabeat

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// ends with '/'
interface Api {
    @POST("/api/register/")
    suspend fun register(@Body obj: Request.Register)
            : Response<Result.Token>

    @POST("/api/token/")
    suspend fun logIn(@Body obj: Request.Login)
            : Response<Result.Token>

    @POST("/api/token/refresh/")
    suspend fun refresh(@Body obj: Request.Refresh)
            : Response<Result.Token>

    @GET("/api/records/")
    suspend fun getRecords(@Header("Authorization") token: String)
            : Response<List<Result.Records>>

    @POST("/api/records/")
    suspend fun postRecord(@Header("Authorization") token: String, @Body obj: Request.Record)
            : Response<Result.Records>

    @Multipart
    @POST("/api/predict/")
    suspend fun predict(@Header("Authorization") token: String, @Part image: MultipartBody.Part)
            : Response<Result.Predict>

    @GET("/api/chat/")
    suspend fun chat(@Header("Authorization") token: String)
            : Response<Result.ChatRoot>
}

object Request {
    data class Register(
        val email: String,
        val username: String,
        val password: String
    )

    data class Login(
        val usernameOrEmail: String,
        val password: String
    )

    data class Refresh(
        val refresh: String
    )

    data class Record(
        val bloodGlucose: Double,
        val carbohydrateIntake: Double?,
        val exerciseDuration: Double?,
        val insulinInjection: Double?
    )
}

object Result {
    data class Token(
        val access: String,
        val refresh: String,
        val username: String?,
        val message: String,
        val success: Boolean
    )

    data class Records(
        val id: Long,
        val user: Long,
        val bloodGlucose: Double,
        val carbohydrateIntake: Double?,
        val exerciseDuration: Double?,
        val insulinInjection: Double?,
        val createdAt: String,
        val timeSlot: String
    )

    data class Predict(
        val predictedValue: Double
    )

    data class ChatRoot(
        val response: Chat
    )

    data class Chat(
        val model: String,
        val createdAt: String,
        val done: Boolean,
        val doneReason: String,
        val totalDuration: Long,
        val loadDuration: Long,
        val promptEvalCount: Long,
        val promptEvalDuration: Long,
        val evalCount: Long,
        val evalDuration: Long,
        val message: ChatMsg
    )

    data class ChatMsg(
        val role: String,
        val content: String,
        val images: String?,
        val toolCalls: String?
    )
}

object Err {
    data class Register(
        var email: List<String>?,
        var username: List<String>?,
    )

    data class Login(
        val nonFieldErrors: List<String>
    )
}