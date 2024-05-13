package sk.prchlik.futured.nasa_app.repository.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import sk.prchlik.futured.nasa_app.model.Meteorite

interface MeteoritesAPI {

    @GET("y77d-th95.json")
    suspend fun getMeteorites(@Query("\$limit") limit: Int?, @Query("name") name: String?): Response<MutableList<Meteorite>>

}