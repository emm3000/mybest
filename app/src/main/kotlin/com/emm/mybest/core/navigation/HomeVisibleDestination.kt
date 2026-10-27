package com.emm.mybest.core.navigation

import com.emm.mybest.domain.usecase.home.HomeDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

private val HOME_LOAD_TIMEOUT = 2.seconds

/**
 * What the Home tab shows right now, after the user dismissed a routed screen. Home publishes it; a launch action
 * reads it. Null until Home has loaded, and again once the Home that published it is gone.
 */
class HomeVisibleDestination {

    private val _destination = MutableStateFlow<HomeDestination?>(null)
    val destination: StateFlow<HomeDestination?> = _destination.asStateFlow()
    private var owner: Any? = null

    /**
     * Makes [owner] the Home that publishes from now on and drops what an older Home published, so a wait sees only
     * the new Home.
     */
    fun claim(owner: Any) {
        this.owner = owner
        _destination.value = null
    }

    fun publish(owner: Any, destination: HomeDestination) {
        this.owner = owner
        _destination.value = destination
    }

    /**
     * Clears the destination only if [owner] published it last, so a Home that is going away never clears what a
     * newer Home already published.
     */
    fun clear(owner: Any) {
        if (this.owner !== owner) return
        this.owner = null
        _destination.value = null
    }

    /**
     * Waits for a Home that is on screen but still loading, at most [HOME_LOAD_TIMEOUT]; null if it never loads.
     */
    suspend fun awaitLoaded(): HomeDestination? =
        withTimeoutOrNull(HOME_LOAD_TIMEOUT) { destination.filterNotNull().first() }
}
