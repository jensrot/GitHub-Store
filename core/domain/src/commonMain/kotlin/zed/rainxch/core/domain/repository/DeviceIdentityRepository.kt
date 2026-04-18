package zed.rainxch.core.domain.repository

interface DeviceIdentityRepository {
    suspend fun getDeviceId(): String

    suspend fun resetDeviceId(): String
}
