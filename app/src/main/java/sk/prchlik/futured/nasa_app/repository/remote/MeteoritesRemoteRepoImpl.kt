package sk.prchlik.futured.nasa_app.repository.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sk.prchlik.futured.nasa_app.communication.CommunicationError
import sk.prchlik.futured.nasa_app.communication.CommunicationResult
import sk.prchlik.futured.nasa_app.model.Meteorite

class MeteoritesRemoteRepoImpl(private val meteoritesAPI: MeteoritesAPI): IMeteoritesRemoteRepo {

    override suspend fun getMeteorites(name: String?): CommunicationResult<MutableList<Meteorite>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                meteoritesAPI.getMeteorites(50000, name)
            }
            if (response.isSuccessful) {
                response.body()?.let {
                    return CommunicationResult.Success(it)
                } ?: kotlin.run {
                    return CommunicationResult.Error(CommunicationError(response.code(), response.errorBody().toString()))
                }
            } else {
                return CommunicationResult.Error(CommunicationError(response.code(), response.errorBody().toString()))
            }
        } catch (ex: Exception) {
            Log.e("REPO", ex.message.toString())
            CommunicationResult.Exception(ex)
        }
    }

}