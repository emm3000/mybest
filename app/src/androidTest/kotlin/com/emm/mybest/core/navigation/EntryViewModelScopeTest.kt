package com.emm.mybest.core.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicInteger

class EntryViewModelScopeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val instanceCounter = AtomicInteger(0)

    private val probeModule: Module = module {
        viewModel { params ->
            EntryProbeViewModel(
                ids = params.values.map { it as String },
                instanceNumber = instanceCounter.incrementAndGet(),
            )
        }
    }

    private lateinit var navigator: Navigator

    @Before
    fun setUp() {
        loadKoinModules(probeModule)
        composeRule.setContent {
            val state = rememberNavigationState(
                startRoute = Screen.Home,
                topLevelRoutes = setOf(Screen.Home, Screen.History),
            )
            navigator = remember { Navigator(state) }
            val provider = remember { probeEntryProvider() }
            NavDisplay(
                entries = state.toEntries(provider),
                onBack = { navigator.goBack() },
            )
        }
    }

    @After
    fun tearDown() {
        unloadKoinModules(probeModule)
    }

    @Test
    fun viewer_showsTheNewPhotoAfterPoppingTheOldOne() {
        navigate(Screen.PhotoViewer("A"))
        composeRule.onNodeWithText("A").assertExists()

        goBack()
        navigate(Screen.PhotoViewer("B"))

        composeRule.onNodeWithText("B").assertExists()
        composeRule.onNodeWithText("A").assertDoesNotExist()
    }

    @Test
    fun compare_showsTheNewPairAfterPoppingTheOldOne() {
        navigate(Screen.ComparePhotos("A", "B"))
        goBack()
        navigate(Screen.ComparePhotos("C", "D"))

        composeRule.onNodeWithText("C").assertExists()
        composeRule.onNodeWithText("D").assertExists()
        composeRule.onNodeWithText("A").assertDoesNotExist()
        composeRule.onNodeWithText("B").assertDoesNotExist()
    }

    @Test
    fun pop_clearsTheEntryViewModel() {
        navigate(Screen.PhotoViewer("A"))
        val firstInstance = instanceText()

        goBack()
        navigate(Screen.PhotoViewer("A"))

        composeRule.onNodeWithText("A").assertExists()
        composeRule.onNodeWithTag(INSTANCE_TAG).assertExists()
        composeRule.onNodeWithText(firstInstance).assertDoesNotExist()
    }

    @Test
    fun tabSwitch_keepsTheEntryViewModel() {
        navigate(Screen.History)
        navigate(Screen.PhotoViewer("A"))
        val firstInstance = instanceText()

        navigate(Screen.Home)
        composeRule.onNodeWithText(HOME_TEXT).assertExists()
        navigate(Screen.History)

        composeRule.onNodeWithText(HISTORY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText("A").assertExists()
        composeRule.onNodeWithText(firstInstance).assertExists()
    }

    private fun navigate(route: NavKey) {
        composeRule.runOnIdle { navigator.navigate(route) }
        composeRule.waitForIdle()
    }

    private fun goBack() {
        composeRule.runOnIdle { navigator.goBack() }
        composeRule.waitForIdle()
    }

    private fun instanceText(): String = INSTANCE_PREFIX + instanceCounter.get()
}

private const val INSTANCE_TAG = "instance"
private const val INSTANCE_PREFIX = "instance #"
private const val HOME_TEXT = "home"
private const val HISTORY_TEXT = "history"

private fun probeEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<Screen.Home> { Text(HOME_TEXT) }
    entry<Screen.History> { Text(HISTORY_TEXT) }
    entry<Screen.PhotoViewer> { key ->
        EntryProbe(koinViewModel { parametersOf(key.initialPhotoId) })
    }
    entry<Screen.ComparePhotos> { key ->
        EntryProbe(koinViewModel { parametersOf(key.beforeId, key.afterId) })
    }
}

@Composable
private fun EntryProbe(viewModel: EntryProbeViewModel) {
    Column {
        viewModel.ids.forEach { id -> Text(id) }
        Text(INSTANCE_PREFIX + viewModel.instanceNumber, Modifier.testTag(INSTANCE_TAG))
    }
}
