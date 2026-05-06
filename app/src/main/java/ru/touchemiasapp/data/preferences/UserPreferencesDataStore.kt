package ru.touchemiasapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val omsNumber: String = "",
    val birthDate: String = ""   // yyyy-MM-dd
) {
    val isComplete: Boolean get() = omsNumber.isNotBlank() && birthDate.isNotBlank()
}

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val OMS_NUMBER = stringPreferencesKey("oms_number")
    private val BIRTH_DATE = stringPreferencesKey("birth_date")

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            omsNumber = prefs[OMS_NUMBER] ?: "",
            birthDate = prefs[BIRTH_DATE] ?: ""
        )
    }

    suspend fun save(omsNumber: String, birthDate: String) {
        context.dataStore.edit { prefs ->
            prefs[OMS_NUMBER] = omsNumber
            prefs[BIRTH_DATE] = birthDate
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
