package sk.prchlik.futured.nasa_app.repository.sync

import kotlinx.coroutines.flow.MutableStateFlow
import sk.prchlik.futured.nasa_app.model.Meteorite
import sk.prchlik.futured.nasa_app.view_model.State

interface ISyncRepo {

    fun syncMeteorites()
    fun refresh()
    fun getState(): MutableStateFlow<State<MutableList<Meteorite>>>

}