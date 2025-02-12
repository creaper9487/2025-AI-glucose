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
    suspend fun register(@Body obj: Request.Register): Response<Result.Token>

    @POST("/api/token/")
    suspend fun logIn(@Body obj: Request.Login): Response<Result.Token>

    @POST("/api/token/refresh/")
    suspend fun refresh(@Body obj: Request.Refresh): Response<Result.Token>

    @GET("/api/records/")
    suspend fun getRecords(@Header("Authorization") token: String): Response<List<Result.Records>>

    @POST("/api/records/")
    suspend fun postRecord(@Header("Authorization") token: String, @Body obj: Request.Record): Response<Result.Records>

    @Multipart
    @POST("/api/predict/")
    suspend fun predict(@Header("Authorization") token: String, @Part image: MultipartBody.Part): Response<Result.Predict>
}

object Request {
    data class Register(
        val email: String,
        val username: String,
        val password: String
    )

    data class Login(
        val username_or_email: String,
        val password: String
    )

    data class Refresh(
        val refresh: String
    )

    data class Record(
        val blood_glucose: Double,
        val carbohydrate_intake: Double?,
        val exercise_duration: Double?,
        val insulin_injection: Double?
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
        val id: Int,
        val user: Int,
        val blood_glucose: Double,
        val carbohydrate_intake: Double?,
        val exercise_duration: Double?,
        val insulin_injection: Double?,
        val created_at: String,
        val time_slot: String
    )

    data class Predict(
        val predicted_value: Double
    )
}

object Err {
    data class Register(
        var email: List<String>?,
        var username: List<String>?,
    )

    data class Login(
        val non_field_errors: List<String>
    )
}