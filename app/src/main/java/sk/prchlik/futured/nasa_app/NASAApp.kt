package sk.prchlik.futured.nasa_app

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import sk.prchlik.futured.nasa_app.di.appModule
import sk.prchlik.futured.nasa_app.di.repositoryModule

class NASAApp: Application() {



    override fun onCreate() {
        super.onCreate()
        // Initialize Koin
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@NASAApp)
            modules(appModule, repositoryModule)
        }
        appContext = applicationContext
    }

    companion object {
        /**
         * Returns the application context.
         *
         * @return
         */
        @JvmStatic
        var appContext: Context? = null
            private set
    }

}