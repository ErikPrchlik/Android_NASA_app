package sk.prchlik.futured.nasa_app.di

import android.app.Application
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import sk.prchlik.futured.nasa_app.BuildConfig
import sk.prchlik.futured.nasa_app.communication.RetrofitConnection
import sk.prchlik.futured.nasa_app.repository.local.IMeteoritesLocalRepo
import sk.prchlik.futured.nasa_app.repository.local.MeteoriteDao
import sk.prchlik.futured.nasa_app.repository.local.MeteoritesLocalRepoImpl
import sk.prchlik.futured.nasa_app.repository.remote.IMeteoritesRemoteRepo
import sk.prchlik.futured.nasa_app.repository.remote.MeteoritesAPI
import sk.prchlik.futured.nasa_app.repository.remote.MeteoritesRemoteRepoImpl
import sk.prchlik.futured.nasa_app.repository.sync.ISyncRepo
import sk.prchlik.futured.nasa_app.repository.sync.SyncRepoImpl
import sk.prchlik.futured.nasa_app.room.AppDatabase
import sk.prchlik.futured.nasa_app.view_model.MapMainActivityVM

val repositoryModule = module {
    single { provideAppToken() }
    single { provideRetrofit(get()) }
    factory { provideMeteoritesAPI(get()) }
    single { provideMeteoritesRemoteRepo(get()) }

    factory { provideMeteoriteDao(androidApplication()) }
    single { provideMeteoritesLocalRepo(get()) }

    single { provideSyncRepo(get(), get()) }
}

val appModule = module {
    viewModel { MapMainActivityVM(androidApplication(), get()) }
}

fun provideAppToken(): String {
    // Retrieve your app token from a secure location
    return BuildConfig.APP_TOKEN
}

fun provideRetrofit(appToken: String): Retrofit {
    return RetrofitConnection.getRetrofit(appToken)
}

fun provideMeteoritesAPI(retrofit: Retrofit): MeteoritesAPI {
    return retrofit.create(MeteoritesAPI::class.java)
}

fun provideMeteoritesRemoteRepo(meteoritesAPI: MeteoritesAPI): IMeteoritesRemoteRepo {
    return MeteoritesRemoteRepoImpl(meteoritesAPI)
}

fun provideMeteoriteDao(app: Application): MeteoriteDao = AppDatabase.getDatabase(app).meteoriteDao()

fun provideMeteoritesLocalRepo(meteoriteDao: MeteoriteDao): IMeteoritesLocalRepo {
    return MeteoritesLocalRepoImpl(meteoriteDao)
}

fun provideSyncRepo(localRepo: IMeteoritesLocalRepo, remoteRepo: IMeteoritesRemoteRepo): ISyncRepo {
    return SyncRepoImpl(localRepo, remoteRepo)
}