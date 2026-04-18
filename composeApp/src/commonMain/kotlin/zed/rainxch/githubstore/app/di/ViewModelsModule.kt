package zed.rainxch.githubstore.app.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import zed.rainxch.apps.presentation.AppsViewModel
import zed.rainxch.auth.presentation.AuthenticationViewModel
import zed.rainxch.details.presentation.DetailsViewModel
import zed.rainxch.devprofile.presentation.DeveloperProfileViewModel
import zed.rainxch.favourites.presentation.FavouritesViewModel
import zed.rainxch.home.presentation.HomeViewModel
import zed.rainxch.profile.presentation.ProfileViewModel
import zed.rainxch.recentlyviewed.presentation.RecentlyViewedViewModel
import zed.rainxch.search.presentation.SearchViewModel
import zed.rainxch.starred.presentation.StarredReposViewModel
import zed.rainxch.tweaks.presentation.TweaksViewModel

val viewModelsModule =
    module {
        viewModelOf(::AppsViewModel)
        viewModelOf(::AuthenticationViewModel)
        viewModel { params ->
            DetailsViewModel(
                repositoryId = params.get(),
                ownerParam = params.get(),
                repoParam = params.get(),
                isComingFromUpdate = params.get(),
                detailsRepository = get(),
                downloader = get(),
                installer = get(),
                platform = get(),
                helper = get(),
                shareManager = get(),
                installedAppsRepository = get(),
                favouritesRepository = get(),
                starredRepository = get(),
                packageMonitor = get(),
                syncInstalledAppsUseCase = get(),
                translationRepository = get(),
                logger = get(),
                tweaksRepository = get(),
                seenReposRepository = get(),
                installationManager = get(),
                attestationVerifier = get(),
                downloadOrchestrator = get(),
                telemetryRepository = get(),
            )
        }
        viewModelOf(::DeveloperProfileViewModel)
        viewModelOf(::FavouritesViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::RecentlyViewedViewModel)
        viewModelOf(::SearchViewModel)
        viewModelOf(::ProfileViewModel)
        viewModelOf(::TweaksViewModel)
        viewModelOf(::StarredReposViewModel)
    }
