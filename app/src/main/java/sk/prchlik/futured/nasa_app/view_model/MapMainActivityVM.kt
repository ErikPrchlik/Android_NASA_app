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
import java.lang.Exception

class MapMainActivityVM(app: Application,
                        private val localRepository: IMeteoritesLocalRepo,
                        private val remoteRepository: IMeteoritesRemoteRepo
                        ): AndroidViewModel(app) {

    // State variables for checking data loading
    private val _meteoritesState: MutableStateFlow<State<MutableList<Meteorite>>>
        = MutableStateFlow(State.Loading)
    val meteoritesState: StateFlow<State<MutableList<Meteorite>>> = _meteoritesState

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

    fun getData() {
        // Request data
        _meteoritesState.value = State.Loading
        viewModelScope.launch {
            try {
                val jobGetLocal = async { getLocalData() }
                val jobGetRemote = async { getRemoteData() }
                val jobCollectData = async { getMeteorites() }
                jobGetLocal.await()
                saveRemoteData(jobGetRemote.await())
                jobCollectData.await()
            } catch (_: Exception) {}
        }
    }

    private suspend fun getMeteorites() {
        // Collecting first loaded data
        viewModelScope.launch {
            _meteoritesState.collect { result ->
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

    private fun getLocalData() {
        // Collect data from ROOM
        var meteorites: MutableList<Meteorite> = mutableListOf()
        viewModelScope.launch {
            meteorites = localRepository.getAll()
        }.invokeOnCompletion {
            if (_meteoritesState.value is State.Loading ||
                _meteoritesState.value is State.Error ||
                _meteoritesState.value is State.NoData) {
                _meteoritesState.value =
                    if (meteorites.isEmpty()) {
                        if (_meteoritesState.value is State.NoData) {
                            State.Error(R.string.communication_error)
                        } else {
                            State.NoData
                        }
                    } else {
                        State.Success(meteorites)
                    }
            }
        }
    }

    private suspend fun getRemoteData() =
        // Collect data from communication with API
        when (val communicationResult = remoteRepository.getMeteorites(null)) {
            is CommunicationResult.Success -> {
                if (_meteoritesState.value is State.Loading ||
                    _meteoritesState.value is State.Error ||
                    _meteoritesState.value is State.NoData) {
                    _meteoritesState.value = State.Success(communicationResult.data)
                }
                State.Success(communicationResult.data)
            }
            is CommunicationResult.Error -> {
                if (_meteoritesState.value is State.NoData) {
                    _meteoritesState.value = State.Error(R.string.communication_error)
                    State.Error(R.string.communication_error)
                } else {
                    _meteoritesState.value = State.NoData
                    State.NoData
                }
            }
            is CommunicationResult.Exception -> {
                if (_meteoritesState.value is State.NoData) {
                    _meteoritesState.value = State.Error(R.string.communication_exception)
                    State.Error(R.string.communication_exception)
                } else {
                    _meteoritesState.value = State.NoData
                    State.NoData
                }
            }
        }

    private suspend fun saveRemoteData(state: State<MutableList<Meteorite>>) {
        // Update of ROOM data by data from API
        when(state) {
            is State.Success -> localRepository.updateAll(state.data)
            is State.Error -> {}
            is State.NoData -> {}
            State.Loading -> {}
        }
    }

}