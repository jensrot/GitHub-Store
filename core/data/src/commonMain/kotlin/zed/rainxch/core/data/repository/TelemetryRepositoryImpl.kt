package zed.rainxch.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import zed.rainxch.core.data.BuildKonfig
import zed.rainxch.core.data.dto.EventRequest
import zed.rainxch.core.data.network.BackendApiClient
import zed.rainxch.core.data.utils.hashQuery
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.model.Platform
import zed.rainxch.core.domain.repository.DeviceIdentityRepository
import zed.rainxch.core.domain.repository.TelemetryRepository
import zed.rainxch.core.domain.repository.TweaksRepository

class TelemetryRepositoryImpl(
    private val backendApiClient: BackendApiClient,
    private val deviceIdentity: DeviceIdentityRepository,
    private val tweaksRepository: TweaksRepository,
    private val platform: Platform,
    private val appScope: CoroutineScope,
    private val logger: GitHubStoreLogger,
) : TelemetryRepository {

    private val bufferMutex = Mutex()
    private val buffer = ArrayDeque<EventRequest>()

    init {
        appScope.launch {
            while (true) {
                delay(FLUSH_INTERVAL_MS)
                runCatching { flushPending() }
                    .onFailure { logger.debug("Telemetry flush error: ${it.message}") }
            }
        }
    }

    // ── recording (fire-and-forget, guarded by opt-in) ──────────────

    override fun recordSearchPerformed(query: String, resultCount: Int) {
        enqueue(
            eventType = "search_performed",
            queryHash = hashQuery(query),
            resultCount = resultCount,
        )
    }

    override fun recordSearchResultClicked(repoId: Long) {
        enqueue(eventType = "search_result_clicked", repoId = repoId)
    }

    override fun recordRepoViewed(repoId: Long) {
        enqueue(eventType = "repo_viewed", repoId = repoId)
    }

    override fun recordReleaseDownloaded(repoId: Long) {
        enqueue(eventType = "release_downloaded", repoId = repoId)
    }

    override fun recordInstallStarted(repoId: Long) {
        enqueue(eventType = "install_started", repoId = repoId)
    }

    override fun recordInstallSucceeded(repoId: Long) {
        enqueue(eventType = "install_succeeded", repoId = repoId, success = true)
    }

    override fun recordInstallFailed(repoId: Long, errorCode: String?) {
        enqueue(
            eventType = "install_failed",
            repoId = repoId,
            success = false,
            errorCode = errorCode,
        )
    }

    override fun recordAppOpenedAfterInstall(repoId: Long) {
        enqueue(eventType = "app_opened_after_install", repoId = repoId)
    }

    override fun recordUninstalled(repoId: Long) {
        enqueue(eventType = "uninstalled", repoId = repoId)
    }

    override fun recordFavorited(repoId: Long) {
        enqueue(eventType = "favorited", repoId = repoId)
    }

    override fun recordUnfavorited(repoId: Long) {
        enqueue(eventType = "unfavorited", repoId = repoId)
    }

    // ── batching ────────────────────────────────────────────────────

    override suspend fun flushPending() {
        val pending = bufferMutex.withLock {
            if (buffer.isEmpty()) return
            val take = minOf(buffer.size, MAX_BATCH_SIZE)
            val batch = (0 until take).map { buffer.removeFirst() }
            batch
        }

        val result = withContext(Dispatchers.IO) {
            backendApiClient.postEvents(pending)
        }

        if (result.isFailure) {
            // Put events back at the front for retry next tick (bounded).
            bufferMutex.withLock {
                for (i in pending.indices.reversed()) {
                    if (buffer.size < MAX_BUFFER_SIZE) buffer.addFirst(pending[i])
                }
            }
            logger.debug("Telemetry batch failed: ${result.exceptionOrNull()?.message}")
        }
    }

    // ── helpers ─────────────────────────────────────────────────────

    private fun enqueue(
        eventType: String,
        repoId: Long? = null,
        queryHash: String? = null,
        resultCount: Int? = null,
        success: Boolean? = null,
        errorCode: String? = null,
    ) {
        appScope.launch {
            val enabled = runCatching { tweaksRepository.getTelemetryEnabled().first() }
                .getOrDefault(false)
            if (!enabled) return@launch

            val deviceId = runCatching { deviceIdentity.getDeviceId() }.getOrNull() ?: return@launch

            val event = EventRequest(
                deviceId = deviceId,
                platform = platformSlug(),
                appVersion = BuildKonfig.VERSION_NAME,
                eventType = eventType,
                repoId = repoId,
                queryHash = queryHash,
                resultCount = resultCount,
                success = success,
                errorCode = errorCode,
            )

            bufferMutex.withLock {
                if (buffer.size >= MAX_BUFFER_SIZE) {
                    buffer.removeFirst()
                }
                buffer.add(event)
            }
        }
    }

    private fun platformSlug(): String = when (platform) {
        Platform.ANDROID -> "android"
        Platform.MACOS -> "desktop-macos"
        Platform.WINDOWS -> "desktop-windows"
        Platform.LINUX -> "desktop-linux"
    }

    private companion object {
        private const val FLUSH_INTERVAL_MS = 30_000L
        private const val MAX_BATCH_SIZE = 50
        private const val MAX_BUFFER_SIZE = 500
    }
}
