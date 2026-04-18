package zed.rainxch.tweaks.presentation

import zed.rainxch.core.domain.model.AppTheme
import zed.rainxch.core.domain.model.FontTheme
import zed.rainxch.core.domain.model.InstallerType
import zed.rainxch.core.domain.model.ProxyScope
import zed.rainxch.core.domain.model.ShizukuAvailability
import zed.rainxch.core.domain.model.TranslationProvider
import zed.rainxch.tweaks.presentation.model.ProxyScopeFormState

data class TweaksState(
    val selectedThemeColor: AppTheme = AppTheme.OCEAN,
    val selectedFontTheme: FontTheme = FontTheme.CUSTOM,
    val isAmoledThemeEnabled: Boolean = false,
    val isDarkTheme: Boolean? = null,
    val versionName: String = "",
    val proxyForms: Map<ProxyScope, ProxyScopeFormState> =
        ProxyScope.entries.associateWith { ProxyScopeFormState() },
    val autoDetectClipboardLinks: Boolean = true,
    val cacheSize: String = "",
    val isClearDownloadsDialogVisible: Boolean = false,
    val installerType: InstallerType = InstallerType.DEFAULT,
    val shizukuAvailability: ShizukuAvailability = ShizukuAvailability.UNAVAILABLE,
    val autoUpdateEnabled: Boolean = false,
    val updateCheckIntervalHours: Long = 6L,
    val includePreReleases: Boolean = false,
    val isLiquidGlassEnabled: Boolean = true,
    val isHideSeenEnabled: Boolean = false,
    val isScrollbarEnabled: Boolean = false,
    val isTelemetryEnabled: Boolean = false,
    val translationProvider: TranslationProvider = TranslationProvider.Default,
    val youdaoAppKey: String = "",
    val youdaoAppSecret: String = "",
    val isYoudaoAppSecretVisible: Boolean = false,
) {
    /** Convenience accessor — returns a fresh default if the map is
     *  missing an entry for [scope]. The constructor seeds all scopes,
     *  but `copy(proxyForms = …)` call sites could in theory produce an
     *  incomplete map; the safe default keeps the UI from crashing in
     *  that case. */
    fun formFor(scope: ProxyScope): ProxyScopeFormState =
        proxyForms[scope] ?: ProxyScopeFormState()
}
