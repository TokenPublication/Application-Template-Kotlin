package com.tokeninc.sardis.application_template.dependency_injection

import com.tokeninc.sardis.application_template.data.repositories.ActivationRepository
import com.tokeninc.sardis.application_template.data.repositories.BatchRepository
import com.tokeninc.sardis.application_template.data.repositories.CardRepository
import com.tokeninc.sardis.application_template.data.repositories.TransactionRepository
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.posttxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
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

    @Provides
    @ViewModelScoped
    fun provideCardViewModel(repository: CardRepository): CardViewModel =
        CardViewModel(repository)
}