package ru.urfu.starwars.presentation.common

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.toUserMessage(): String = when (this) {
    is SocketTimeoutException -> "Сервер долго не отвечает. Попробуйте ещё раз"
    is IOException -> "Нет подключения к интернету"
    is HttpException -> when (code()) {
        404 -> "Ничего не найдено"
        in 500..599 -> "Сервер SWAPI недоступен (${code()})"
        else -> "Ошибка сервера: ${code()}"
    }
    else -> "Что-то пошло не так: ${message ?: javaClass.simpleName}"
}
