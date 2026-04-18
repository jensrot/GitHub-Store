package zed.rainxch.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import zed.rainxch.core.domain.repository.DeviceIdentityRepository

@OptIn(ExperimentalUuidApi::class)
class DeviceIdentityRepositoryImpl(
    private val preferences: DataStore<Preferences>,
) : DeviceIdentityRepository {

    override suspend fun getDeviceId(): String {
        val existing = preferences.data.first()[DEVICE_ID_KEY]
        if (!existing.isNullOrBlank()) return existing

        val generated = Uuid.random().toString()
        preferences.edit { it[DEVICE_ID_KEY] = generated }
        return generated
    }

    override suspend fun resetDeviceId(): String {
        val next = Uuid.random().toString()
        preferences.edit { it[DEVICE_ID_KEY] = next }
        return next
    }

    private companion object {
        private val DEVICE_ID_KEY = stringPreferencesKey("anonymous_device_id")
    }
}
