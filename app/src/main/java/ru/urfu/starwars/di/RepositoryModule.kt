package ru.urfu.starwars.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.urfu.starwars.data.repository.PeopleRepositoryImpl
import ru.urfu.starwars.domain.repository.PeopleRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindPeopleRepository(impl: PeopleRepositoryImpl): PeopleRepository
}
