package de.openclaw.assistant.util

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorHandler {

    fun handle(throwable: Throwable): AppError {
        return when (throwable) {
            is SocketTimeoutException -> AppError.Network.Timeout
            is UnknownHostException -> AppError.Network.NoConnection
            is IOException -> AppError.Network.IOError(throwable.message)
            is HttpException -> handleHttpException(throwable)
            else -> AppError.Unknown(throwable.message)
        }
    }

    private fun handleHttpException(exception: HttpException): AppError {
        return when (exception.code()) {
            400 -> AppError.Api.BadRequest(exception.message())
            401 -> AppError.Api.Unauthorized
            403 -> AppError.Api.Forbidden
            404 -> AppError.Api.NotFound
            429 -> AppError.Api.RateLimited
            500, 502, 503, 504 -> AppError.Api.ServerError(exception.code())
            else -> AppError.Api.Unknown(exception.code(), exception.message())
        }
    }

    fun getUserFriendlyMessage(error: AppError): String {
        return when (error) {
            is AppError.Network.Timeout -> "Verbindung Zeitüberschreitung. Bitte versuche es erneut."
            is AppError.Network.NoConnection -> "Keine Internetverbindung. Prüfe deine Verbindung."
            is AppError.Network.IOError -> "Netzwerkfehler. Bitte versuche es später."
            is AppError.Api.Unauthorized -> "Nicht autorisiert. Bitte melde dich erneut an."
            is AppError.Api.ServerError -> "Server-Fehler. Wir arbeiten daran."
            is AppError.Api.RateLimited -> "Zu viele Anfragen. Bitte warte einen Moment."
            else -> "Ein Fehler ist aufgetreten. Bitte versuche es erneut."
        }
    }
}

sealed class AppError {
    sealed class Network : AppError() {
        object Timeout : Network()
        object NoConnection : Network()
        data class IOError(val message: String?) : Network()
    }

    sealed class Api : AppError() {
        data class BadRequest(val message: String?) : Api()
        object Unauthorized : Api()
        object Forbidden : Api()
        object NotFound : Api()
        object RateLimited : Api()
        data class ServerError(val code: Int) : Api()
        data class Unknown(val code: Int, val message: String?) : Api()
    }

    data class Unknown(val message: String?) : AppError()
}
