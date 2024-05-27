package sk.prchlik.futured.nasa_app.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sk.prchlik.futured.nasa_app.model.Meteorite
import sk.prchlik.futured.nasa_app.repository.local.MeteoriteDao

@Database(
    entities = [Meteorite::class],
    version = 1
)

@TypeConverters(MapConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun meteoriteDao(): MeteoriteDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "app_database"
                        )
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}