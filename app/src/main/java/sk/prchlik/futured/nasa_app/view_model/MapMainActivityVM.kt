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

class MapMainActivityVM(private val app: Application,
                        private val localRepository: IMeteoritesLocalRepo,
                        private val remoteRepository: IMeteoritesRemoteRepo
): AndroidViewModel(app) {


    private val _meteoritesState: MutableStateFlow<State<MutableList<Meteorite>>>
        = MutableStateFlow(State.Loading)
    val meteoritesState: StateFlow<State<MutableList<Meteorite>>> = _meteoritesState

    private val _dataFlow = MutableSharedFlow<MutableList<Meteorite>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val dataFlow: Flow<MutableList<Meteorite>> = _dataFlow

    init {
        _meteoritesState.value = State.Loading
        viewModelScope.launch {
            try {
                val jobGetLocal = async { getLocalData() }
                val jobGetRemote = async { getRemoteData(null) }
                val jobCollectData = async { getMeteorites(null) }
                jobGetLocal.await()
                saveRemoteData(jobGetRemote.await())
                jobCollectData.await()
            } catch (_: Exception) {}
        }
    }

    private suspend fun getMeteorites(name: String?) {
        viewModelScope.launch {
            _meteoritesState.collect { result ->
                when(result) {
                    is State.Success -> {
                        _dataFlow.tryEmit(result.data)
                    }
                    is State.Error -> _dataFlow.tryEmit(mutableListOf())
                    State.Loading -> {}
                }
            }
        }
    }

    private fun getLocalData() {
        var meteorites: MutableList<Meteorite> = mutableListOf()
        viewModelScope.launch {
            meteorites = localRepository.getAll()
        }.invokeOnCompletion {
            if (_meteoritesState.value is State.Loading ||
                _meteoritesState.value is State.Error) {
                _meteoritesState.value =
                    if (meteorites.isEmpty()) {
                        State.Error(R.string.communication_error)
                    } else {
                        State.Success(meteorites)
                    }
            }
        }
    }

    private suspend fun getRemoteData(name: String?) =
        when (val communicationResult = remoteRepository.getMeteorites(name)) {
            is CommunicationResult.Success -> {
                if (_meteoritesState.value is State.Loading ||
                    _meteoritesState.value is State.Error) {
                    _meteoritesState.value = State.Success(communicationResult.data)
                }
                State.Success(communicationResult.data)
            }
            is CommunicationResult.Error -> {
                _meteoritesState.value = State.Error(R.string.communication_error)
                State.Error(R.string.communication_error)
            }
            is CommunicationResult.Exception -> {
                _meteoritesState.value = State.Error(R.string.communication_error)
                State.Error(R.string.communication_exception)
            }
        }

    private suspend fun saveRemoteData(state: State<MutableList<Meteorite>>) {
        when(state) {
            is State.Success -> localRepository.updateAll(state.data)
            is State.Error -> {}
            State.Loading -> {}
        }
    }

}