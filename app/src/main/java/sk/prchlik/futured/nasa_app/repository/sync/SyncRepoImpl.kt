package sk.prchlik.futured.nasa_app.repository.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sk.prchlik.futured.nasa_app.R
import sk.prchlik.futured.nasa_app.communication.CommunicationResult
import sk.prchlik.futured.nasa_app.model.Meteorite
import sk.prchlik.futured.nasa_app.repository.local.IMeteoritesLocalRepo
import sk.prchlik.futured.nasa_app.repository.remote.IMeteoritesRemoteRepo
import sk.prchlik.futured.nasa_app.view_model.State
import java.lang.Exception

class SyncRepoImpl(private val localRepository: IMeteoritesLocalRepo,
                   private val remoteRepository: IMeteoritesRemoteRepo): ISyncRepo {

    // State variables for checking data loading
    private val _meteoritesState: MutableStateFlow<State<MutableList<Meteorite>>>
            = MutableStateFlow(State.Loading)
    val meteoritesState: StateFlow<State<MutableList<Meteorite>>> = _meteoritesState

    override fun syncMeteorites() {
        // Request data
        _meteoritesState.value = State.Loading
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            try {
                val jobGetLocal = async { getLocalData() }
                val jobGetRemote = async { getRemoteData() }
                jobGetLocal.await()
                saveRemoteData(jobGetRemote.await())
            } catch (_: Exception) {}
        }
    }

    private suspend fun getLocalData() {
        // Collect data from ROOM
        var meteorites: MutableList<Meteorite> = mutableListOf()
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
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

    override fun refresh() {
        syncMeteorites()
    }

    override fun getState(): MutableStateFlow<State<MutableList<Meteorite>>> {
        return _meteoritesState
    }
}