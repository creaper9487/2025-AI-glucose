package rl.collab.diabeat

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// ends with '/'
interface Api {
    @POST("/api/register/")
    suspend fun register(@Body request: Request.Register): Response<Result.Token>

    @POST("/api/token/")
    suspend fun logIn(@Body request: Request.LogIn): Response<Result.Token>
}

object Request {
    data class Register(
        val email: String,
        val username: String,
        val password: String
    )

    data class LogIn(
        val username_or_email: String,
        val password: String
    )
}

object Result {
    data class Token(
        val refresh: String,
        val access: String,
        val message: String,
        val success: Boolean
    )
}

object Err {
    data class Register(
        var email: List<String>? = null,
        var username: List<String>? = null,
    )

    data class LogIn(
        val non_field_errors: List<String>
    )
}