package sk.prchlik.futured.nasa_app.view_model

import androidx.annotation.StringRes

sealed class State<out T: Any> {
    class Success<out T: Any>(val data: T): State<T>()
    class Error(@StringRes val errorMessage: Int): State<Nothing>()
    data object NoData: State<Nothing>()
    data object Loading: State<Nothing>()
}