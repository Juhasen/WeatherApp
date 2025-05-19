package pl.juhas.weatherapp.api

import pl.juhas.weatherapp.api.model.GeoLocationModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoAPI {

    @GET("direct")
    suspend fun getGeoLocation(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("limit") limit: Int = 10,
    ) : Response<GeoLocationModel>

}