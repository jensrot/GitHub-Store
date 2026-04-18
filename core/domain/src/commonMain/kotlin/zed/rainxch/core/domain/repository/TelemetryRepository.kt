package zed.rainxch.core.domain.repository

interface TelemetryRepository {
    fun recordSearchPerformed(query: String, resultCount: Int)

    fun recordSearchResultClicked(repoId: Long)

    fun recordRepoViewed(repoId: Long)

    fun recordReleaseDownloaded(repoId: Long)

    fun recordInstallStarted(repoId: Long)

    fun recordInstallSucceeded(repoId: Long)

    fun recordInstallFailed(repoId: Long, errorCode: String?)

    fun recordAppOpenedAfterInstall(repoId: Long)

    fun recordUninstalled(repoId: Long)

    fun recordFavorited(repoId: Long)

    fun recordUnfavorited(repoId: Long)

    suspend fun flushPending()
}
