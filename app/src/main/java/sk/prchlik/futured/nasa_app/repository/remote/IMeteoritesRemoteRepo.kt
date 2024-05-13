package sk.prchlik.futured.nasa_app.repository.remote

import sk.prchlik.futured.nasa_app.communication.CommunicationResult
import sk.prchlik.futured.nasa_app.model.Meteorite

interface IMeteoritesRemoteRepo {

    suspend fun getMeteorites(name: String?): CommunicationResult<MutableList<Meteorite>>

}