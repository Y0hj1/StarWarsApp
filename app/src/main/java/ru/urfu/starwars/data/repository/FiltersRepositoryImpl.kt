package ru.urfu.starwars.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.urfu.starwars.domain.model.GenderFilter
import ru.urfu.starwars.domain.model.PeopleFilter
import ru.urfu.starwars.domain.repository.FiltersRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Фильтры живут в DataStore и переживают перезапуск приложения. */
@Singleton
class FiltersRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : FiltersRepository {

    private object Keys {
        val NAME = stringPreferencesKey("filter_name")
        val GENDER = stringPreferencesKey("filter_gender")
        val PILOTS_ONLY = booleanPreferencesKey("filter_pilots_only")
    }

    override fun observeFilter(): Flow<PeopleFilter> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            PeopleFilter(
                nameQuery = prefs[Keys.NAME].orEmpty(),
                gender = prefs[Keys.GENDER]
                    ?.let { name -> GenderFilter.entries.firstOrNull { it.name == name } }
                    ?: GenderFilter.ANY,
                pilotsOnly = prefs[Keys.PILOTS_ONLY] ?: false,
            )
        }
        .distinctUntilChanged()

    override suspend fun saveFilter(filter: PeopleFilter) {
        dataStore.edit { prefs ->
            prefs[Keys.NAME] = filter.nameQuery
            prefs[Keys.GENDER] = filter.gender.name
            prefs[Keys.PILOTS_ONLY] = filter.pilotsOnly
        }
    }
}
