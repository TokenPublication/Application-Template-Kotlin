package com.tokeninc.sardis.application_template.dependency_injection

import android.app.Application
import com.tokeninc.sardis.application_template.database.AppTempDB
import com.tokeninc.sardis.application_template.database.entities.Batch
import com.tokeninc.sardis.application_template.repositories.ActivationRepository
import com.tokeninc.sardis.application_template.repositories.BatchRepository
import com.tokeninc.sardis.application_template.repositories.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** This is a Module for Hilt, Hilt has some modules to define some values. If you define those values there, you
 * don't need to define them everytime to call those. The only thing to do is @Inject method before you call those values
 * If you Inject them in constructor you need to @Inject constructor, else if in class first you should
 * annotate that class with @AndroidEntryPoint then you specify its class and add @Inject annotation to head of that.
 * Because there are different lifecycles in Android, in @InstallIn parameter you specify
 * to get more details you can visit https://developer.android.com/training/dependency-injection/hilt-android
 * lifecycle of values that those class defines
 * This is SingletonComponent because those values should live as application does
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** It returns a AppTempDB instance
     * @Provide is for providing dependency which class it returns.
     * To do that without errors, You should specify only one provide method for each class
     * otherwise you should named them and call them with their names.
     * @Singleton is for making it single instance of this. If it doesn't exist, everytime we call this it creates new instance
     * @param app is Application, as I mentioned in AppTemp class, it comes from there and returns ActivityContext
     */
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppTempDB = AppTempDB.getInstance(app)
    //app TestApplicationda @HiltAndroidApp dediğimiz için oradan geldi Application

    /**
     * It returns Repository instance
     * @param database is comes from provideDatabase method automatically
     */
    @Provides
    @Singleton
    fun provideActivationRepository(database: AppTempDB): ActivationRepository = ActivationRepository(database.activationDao)

    @Provides
    @Singleton
    fun provideBatchRepository(database: AppTempDB): BatchRepository = BatchRepository(database.batchDao)

    @Provides
    @Singleton
    fun provideTransactionRepository(database: AppTempDB): TransactionRepository = TransactionRepository(database.transactionDao)


}