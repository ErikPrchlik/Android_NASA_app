package sk.prchlik.futured.nasa_app.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sk.prchlik.futured.nasa_app.R
import sk.prchlik.futured.nasa_app.communication.CommunicationResult
import sk.prchlik.futured.nasa_app.model.Meteorite
import sk.prchlik.futured.nasa_app.repository.local.IMeteoritesLocalRepo
import sk.prchlik.futured.nasa_app.repository.remote.IMeteoritesRemoteRepo
import sk.prchlik.futured.nasa_app.repository.sync.ISyncRepo
import sk.prchlik.futured.nasa_app.repository.sync.SyncRepoImpl
import java.lang.Exception

class MapMainActivityVM(app: Application,
                        private val syncRepo: ISyncRepo
                        ): AndroidViewModel(app) {

    // State variables for updating UI
    private val _dataFlow = MutableSharedFlow<MutableList<Meteorite>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val dataFlow: Flow<MutableList<Meteorite>> = _dataFlow

    init {
        // On the injection of VM to activity running tasks parallel on background thread
        getData()
    }

    fun sync() {
        syncRepo.syncMeteorites()
    }

    private fun getData() {
        // Request data
        viewModelScope.launch {
            try {
                val jobCollectData = async { getMeteorites() }
                jobCollectData.await()
            } catch (_: Exception) {}
        }
    }

    fun refreshData() {
        syncRepo.refresh()
    }

    private suspend fun getMeteorites() {
        // Collecting first loaded data
        viewModelScope.launch {
            syncRepo.getState().collect { result ->
                when(result) {
                    is State.Success -> {
                        _dataFlow.tryEmit(result.data)
                    }
                    is State.Error -> _dataFlow.tryEmit(mutableListOf())
                    is State.NoData -> {}
                    State.Loading -> {}
                }
            }
        }
    }

}