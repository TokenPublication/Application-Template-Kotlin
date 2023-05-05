package com.tokeninc.sardis.application_template.dependency_injection

import android.app.Application
import com.tokeninc.sardis.application_template.repositories.ActivationRepository
import com.tokeninc.sardis.application_template.repositories.BatchRepository
import com.tokeninc.sardis.application_template.repositories.TransactionRepository
import com.tokeninc.sardis.application_template.viewmodels.ActivationViewModel
import com.tokeninc.sardis.application_template.viewmodels.BatchViewModel
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/** This is a module object for Dependency Injection with Hilt
 * It defines the viewModel inside that object
 * Because ViewModel's life is different than App's Life in lifecycle we have to define it another module
 * @InstallIn(ViewModelComponent::class) means it's life as much as ViewModel's life
 */
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    /**
     * It defines our viewModel, app and repository are gotten from Hilt DI
     */
    @Provides
    @ViewModelScoped
    fun provideActivationViewModel(repository: ActivationRepository): ActivationViewModel =
        ActivationViewModel(repository)

    @Provides
    @ViewModelScoped
    fun provideBatchViewModel(repository: BatchRepository): BatchViewModel =
        BatchViewModel(repository)

    @Provides
    @ViewModelScoped
    fun provideTransactionViewModel(repository: TransactionRepository): TransactionViewModel =
        TransactionViewModel(repository)
}