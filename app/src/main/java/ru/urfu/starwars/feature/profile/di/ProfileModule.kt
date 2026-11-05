package ru.urfu.starwars.feature.profile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.urfu.starwars.feature.profile.data.ProfileRepositoryImpl
import ru.urfu.starwars.feature.profile.domain.repository.ProfileRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {
    @Binds
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
