package com.example.crisisconnect.data

import com.example.crisisconnect.data.network.SupabaseHttpClient
import kotlinx.serialization.Serializable

/**
 * Thin auth façade so UI does not depend directly on Supabase APIs.
 */
object AuthRepository {

    /**
     * Email + password registration.
     * Returns user info on success.
     */
    suspend fun signUp(email: String, password: String): AuthResponse {
        return SupabaseHttpClient.postAuth<AuthResponse>(
            path = "/auth/v1/signup",
            body = SignUpRequest(email = email, password = password)
        )
    }

    /**
     * Email + password sign in.
     * Returns user info and access token on success.
     */
    suspend fun signIn(email: String, password: String): AuthResponse {
        return SupabaseHttpClient.postAuth<AuthResponse>(
            path = "/auth/v1/token?grant_type=password",
            body = SignInRequest(email = email, password = password)
        )
    }

    /**
     * Request password reset email.
     */
    suspend fun resetPassword(email: String) {
        SupabaseHttpClient.postAuth<Unit>(
            path = "/auth/v1/recover",
            body = EmailRequest(email = email)
        )
    }

    /**
     * Verify email OTP (for signup / email confirmation).
     */
    suspend fun verifyOtp(email: String, token: String): AuthResponse {
        return SupabaseHttpClient.postAuth<AuthResponse>(
            path = "/auth/v1/verify",
            body = VerifyOtpRequest(
                type = "signup",
                email = email,
                token = token
            )
        )
    }

    /**
     * Google sign-in, using an ID token from GoogleSignIn on Android.
     */
    suspend fun signInWithGoogle(idToken: String): AuthResponse {
        return SupabaseHttpClient.postAuth<AuthResponse>(
            path = "/auth/v1/token?grant_type=id_token",
            body = GoogleSignInRequest(
                provider = "google",
                id_token = idToken
            )
        )
    }

    /**
     * Sign out current user.
     */
    suspend fun signOut() {
        // For REST API, we typically just clear local tokens
        // The actual logout is handled client-side by clearing the session
    }
}

@Serializable
data class AuthResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val expires_in: Long? = null,
    val refresh_token: String? = null,
    val user: UserInfo? = null
)

@Serializable
data class UserInfo(
    val id: String,
    val email: String? = null,
    val phone: String? = null,
    val confirmed_at: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
private data class SignInRequest(
    val email: String,
    val password: String
)

@Serializable
private data class SignUpRequest(
    val email: String,
    val password: String
)

@Serializable
private data class EmailRequest(
    val email: String
)

@Serializable
private data class VerifyOtpRequest(
    val type: String,
    val email: String,
    val token: String
)

@Serializable
private data class GoogleSignInRequest(
    val provider: String,
    val id_token: String
)


