package sk.prchlik.futured.nasa_app.communication

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sk.prchlik.futured.nasa_app.BuildConfig

object RetrofitConnection {

    fun getRetrofit(appToken: String): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("\$\$app_token", appToken)
                    .method(original.method(), original.body())
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

//    fun getAuthRetrofit(): Retrofit {
//        val logging = HttpLoggingInterceptor()
//        logging.level = HttpLoggingInterceptor.Level.BODY
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(logging)
//
//        return Retrofit.Builder()
//            .baseUrl(BuildConfig.AUTH_URL)
//            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
//            .client(okHttpClient.build())
//            .build()
//    }
}