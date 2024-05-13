package sk.prchlik.futured.nasa_app.repository.local

import sk.prchlik.futured.nasa_app.model.Meteorite

interface IMeteoritesLocalRepo {

    suspend fun getAll(): MutableList<Meteorite>
    suspend fun get(id: Long): Meteorite
    suspend fun insertAll(meteorites: List<Meteorite>)
    suspend fun deleteAll()
    suspend fun updateAll(meteorites: List<Meteorite>)

}