package com.example.individual_project.utils

/**
 * Wraps every async operation result flowing from DataSource → Repository → ViewModel.
 * ViewModels expose [Loading] while work is in-flight, then [Success] or [Error].
 * DataSources and Repositories only return [Success] or [Error] — never [Loading].
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T)                                        : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null)      : Resource<Nothing>()
    object Loading                                                            : Resource<Nothing>()
}

inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) action(data)
    return this
}

inline fun <T> Resource<T>.onError(action: (String, Throwable?) -> Unit): Resource<T> {
    if (this is Resource.Error) action(message, cause)
    return this
}
