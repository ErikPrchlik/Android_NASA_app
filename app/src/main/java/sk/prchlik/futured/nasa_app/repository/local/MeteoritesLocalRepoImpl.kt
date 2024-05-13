package sk.prchlik.futured.nasa_app.repository.local

import sk.prchlik.futured.nasa_app.model.Meteorite

class MeteoritesLocalRepoImpl(private val dao: MeteoriteDao): IMeteoritesLocalRepo {

    override suspend fun getAll(): MutableList<Meteorite> {
        return dao.getAll()
    }

    override suspend fun get(id: Long): Meteorite {
        return dao.get(id)
    }

    override suspend fun insertAll(meteorites: List<Meteorite>) {
        dao.insertAll(meteorites)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun updateAll(meteorites: List<Meteorite>) {
        dao.updateAll(meteorites)
    }
}