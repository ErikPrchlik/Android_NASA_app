package sk.prchlik.futured.nasa_app.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import sk.prchlik.futured.nasa_app.model.Meteorite

@Dao
interface MeteoriteDao {

    @Query("SELECT * FROM meteorites")
    suspend fun getAll(): MutableList<Meteorite>

    @Query("SELECT * FROM meteorites WHERE id = :id")
    suspend fun get(id: Long): Meteorite

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meteorite: List<Meteorite>)

    @Query("DELETE FROM meteorites")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateAll(meteorites: List<Meteorite>) {
        deleteAll()
        insertAll(meteorites)
    }

}
