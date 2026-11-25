package com.twohearts.app.ui.navigation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.data.repository.PeriodEntryRepository
import com.twohearts.app.data.repository.PeriodSettingsRepository
import com.twohearts.app.data.repository.generateId
import com.twohearts.app.data.repository.nowUtc
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.ui.screens.period.*
import com.twohearts.app.ui.components.useToast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal fun NavGraphBuilder.periodRoutes(
    nav: NavHostController,
    relationship: RelationshipService,
    entries: PeriodEntryRepository,
    settings: PeriodSettingsRepository,
) {
    val back: () -> Unit = { nav.popBackStack() }
    val edit: (PeriodEntry) -> Unit = { nav.navigate("${RoutePath.APP_PERIOD}/${Uri.encode(it.id)}/edit") }

    composable(RoutePath.APP_PERIOD) {
        val latest = latestLoggedPeriod(privateEntries(relationship, entries), java.time.LocalDate.now())
        val saved by remember { settings.observe() }.collectAsStateWithLifecycle(initialValue = null)
        PeriodHome(
            latestEntry = latest,
            onLogPeriod = { nav.navigate(RoutePath.APP_PERIOD_LOG) },
            onViewCalendar = { nav.navigate(RoutePath.APP_PERIOD_CALENDAR) },
            onViewHistory = { nav.navigate(RoutePath.APP_PERIOD_HISTORY) },
            onSettings = { nav.navigate(RoutePath.APP_PERIOD_SETTINGS) },
            onBack = back,
            cycleLength = saved?.cycleLengthDays ?: 28,
            onEditLatest = latest?.let { { edit(it) } },
        )
    }

    composable(RoutePath.APP_PERIOD_LOG) {
        PeriodEntryForm(null, back, entries) { start, end, flow, note ->
            val owner = checkNotNull(relationship.getOwner())
            val now = nowUtc()
            entries.create(PeriodEntry(
                startDate = start, endDate = end, flowLevel = flow, note = note,
                profileId = owner.id, id = generateId(), createdAt = now, updatedAt = now,
            ))
        }
    }

    composable(RoutePath.APP_PERIOD_CALENDAR) {
        val saved by remember { settings.observe() }.collectAsStateWithLifecycle(initialValue = null)
        PeriodCalendarScreen(
            entries = privateEntries(relationship, entries),
            onBack = back,
            cycleLength = saved?.cycleLengthDays ?: 28,
            periodLength = saved?.periodLengthDays ?: 5,
        )
    }

    composable(RoutePath.APP_PERIOD_HISTORY) {
        PeriodHistoryScreen(privateEntries(relationship, entries), back, onEdit = edit)
    }

    composable(RoutePath.APP_PERIOD_SETTINGS) {
        val feedback = useToast()
        val scope = rememberCoroutineScope()
        val saved by remember { settings.observe() }.collectAsStateWithLifecycle(initialValue = null)
        var saving by remember { mutableStateOf(false) }
        var failed by remember { mutableStateOf(false) }
        BackHandler(saving) {}
        PeriodSettingsScreen(
            onBack = { if (!saving) back() },
            initialCycle = saved?.cycleLengthDays ?: 28,
            initialLength = saved?.periodLengthDays ?: 5,
            saving = saving,
            error = if (failed) "Couldn’t save cycle settings. Try again." else null,
            onSave = { cycle, period ->
                if (!saving) {
                    saving = true
                    failed = false
                    scope.launch {
                        try {
                            settings.saveLengths(cycle, period)
                            feedback.success("Cycle settings saved.")
                            back()
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            failed = true
                        } finally {
                            saving = false
                        }
                    }
                }
            },
        )
    }

    composable("${RoutePath.APP_PERIOD}/{entryId}/edit") { route ->
        val id = route.arguments?.getString("entryId") ?: return@composable
        val lookup = rememberRecordLookup(id) {
            val ownerId = relationship.getOwner()?.id
            entries.getById(id)?.takeIf { it.belongsToPeriodOwner(ownerId) }
        }
        val selected = lookup.record
        if (selected != null) {
            PeriodEntryForm(selected, back, entries) { start, end, flow, note ->
                checkNotNull(entries.update(id, mapOf(
                    "startDate" to start, "endDate" to end, "flowLevel" to flow, "note" to note,
                )))
            }
        } else {
            RecordLookupScreen("Period entry", lookup, back)
        }
    }
}

@Composable
private fun privateEntries(
    relationship: RelationshipService,
    repository: PeriodEntryRepository,
): List<PeriodEntry> {
    val owner by remember(relationship) { relationship.observeOwner() }.collectAsStateWithLifecycle(initialValue = null)
    val entries by remember(repository) { repository.observeAll() }.collectAsStateWithLifecycle(initialValue = emptyList())
    return remember(entries, owner?.id) { entries.filter { it.belongsToPeriodOwner(owner?.id) } }
}

@Composable
private fun PeriodEntryForm(
    entry: PeriodEntry?,
    back: () -> Unit,
    repository: PeriodEntryRepository,
    save: suspend (String, String?, String, String?) -> Unit,
) {
    val feedback = useToast()
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    BackHandler(saving) {}

    fun submit(success: String, failure: String, action: suspend () -> Unit) {
        if (saving) return
        saving = true
        error = null
        scope.launch {
            try {
                action()
                feedback.success(success)
                back()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                error = failure
            } finally {
                saving = false
            }
        }
    }

    LogPeriod(
        entryId = entry?.id,
        initialStartDate = entry?.startDate ?: java.time.LocalDate.now().toString(),
        initialEndDate = entry?.endDate.orEmpty(),
        initialFlowLevel = entry?.flowLevel ?: "medium",
        initialNote = entry?.note.orEmpty(),
        onBack = { if (!saving) back() },
        onSave = { start, end, flow, note ->
            submit("Period entry saved.", "Couldn’t save this entry. Try again.") { save(start, end, flow, note) }
        },
        onDelete = entry?.let { { submit("Period entry deleted.", "Couldn’t delete this entry. Try again.") { repository.softDelete(it.id) } } },
        saving = saving,
        error = error,
    )
}
