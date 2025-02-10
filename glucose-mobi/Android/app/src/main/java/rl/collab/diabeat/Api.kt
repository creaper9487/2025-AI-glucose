package rl.collab.diabeat

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// ends with '/'
interface Api {
    @POST("/api/register/")
    suspend fun register(@Body request: Request.Register): Response<Result.Token>

    @POST("/api/token/")
    suspend fun logIn(@Body request: Request.Login): Response<Result.Token>

    @POST("/api/token/refresh/")
    suspend fun refresh(@Body request: Request.Refresh): Response<Result.Token>

    @GET("/api/records/")
    suspend fun getRecords(@Header("Authorization") token: String): Response<List<Result.Records>>

    @POST("/api/records/")
    suspend fun postRecords(@Header("Authorization") token: String, @Body request: Request.Records): Response<Result.Records>

    @POST("/api/predict/")
    suspend fun predict(@Header("Authorization") token: String, @Body request: Request.Predict): Response<Result.Predict>
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

    data class Predict(
        val image: String
    )

    data class Records(
        val carbohydrate_intake: Double?,
        val blood_glucose: Double,
        val exercise_duration: Double?,
        val insulin_injection: Double?
    )
}

object Result {
    data class Token(
        val refresh: String,
        val access: String,
        val username: String?,
        val message: String,
        val success: Boolean
    )

    data class Predict(
        val predicted_value: Double
    )

    data class Records(
        val id: Int,
        val user: Int,
        val carbohydrate_intake: Double?,
        val blood_glucose: Double,
        val exercise_duration: Double?,
        val insulin_injection: Double?,
        val created_at: String,
        val time_slot: String
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