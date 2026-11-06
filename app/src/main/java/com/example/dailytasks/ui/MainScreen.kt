package com.example.dailytasks.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailytasks.R
import com.example.dailytasks.data.TaskEntity
import com.example.dailytasks.data.RepeatRule
import com.example.dailytasks.ui.theme.AccentOptions
import com.example.dailytasks.ui.theme.ThemeMode
import com.example.dailytasks.ui.theme.taskColor
import com.example.dailytasks.util.DayRelation
import com.example.dailytasks.util.MonthCell
import com.example.dailytasks.util.TaskDateUtils
import com.example.dailytasks.util.TaskGroup
import com.example.dailytasks.util.SortMode
import com.example.dailytasks.util.dateRelation
import com.example.dailytasks.util.groupTasks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: TaskViewModel = viewModel(),
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentKey: String = AccentOptions.first().key,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onAccentChange: (String) -> Unit = {}
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val nowMillis = rememberCurrentTimeMillis()
    val todayKey = TaskDateUtils.dayKey(nowMillis)
    val todayTasks = remember(tasks, todayKey) {
        tasks.filter { TaskDateUtils.dayKey(it.dueAt) == todayKey }
    }
    val tasksByDay = remember(tasks) {
        tasks.groupBy { TaskDateUtils.dayKey(it.dueAt) }
    }
    val weekCells = remember(nowMillis) { TaskDateUtils.buildWeekCells(nowMillis) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showCalendar by rememberSaveable { mutableStateOf(false) }
    var calendarMonth by rememberSaveable {
        mutableStateOf(TaskDateUtils.firstDayOfMonth(System.currentTimeMillis()))
    }
    var selectedDateKey by rememberSaveable {
        mutableStateOf(TaskDateUtils.dayKey(System.currentTimeMillis()))
    }
    var editorVisible by rememberSaveable { mutableStateOf(false) }
    var editingTaskId by rememberSaveable { mutableStateOf<Long?>(null) }
    var taskPendingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var settingsVisible by rememberSaveable { mutableStateOf(false) }
    var statsVisible by rememberSaveable { mutableStateOf(false) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var sortModeIndex by rememberSaveable { mutableIntStateOf(SortMode.TIME.ordinal) }
    val sortMode = SortMode.entries[sortModeIndex]

    var searchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable { mutableStateOf(STATUS_ALL) }
    var dateFilterKey by rememberSaveable { mutableStateOf<String?>(null) }

    val editingTask = editingTaskId?.let { id -> tasks.firstOrNull { it.id == id } }
    val taskPendingDelete = taskPendingDeleteId?.let { id -> tasks.firstOrNull { it.id == id } }

    val visibleTasks = remember(tasks, searchQuery, statusFilter, dateFilterKey) {
        val query = searchQuery.trim()
        tasks.filter { task ->
            val matchesQuery = query.isEmpty() ||
                task.title.contains(query, ignoreCase = true) ||
                task.note.contains(query, ignoreCase = true)
            val matchesStatus = when (statusFilter) {
                STATUS_UNDONE -> !task.isCompleted
                STATUS_DONE -> task.isCompleted
                else -> true
            }
            val matchesDate = dateFilterKey == null ||
                TaskDateUtils.dayKey(task.dueAt) == dateFilterKey
            matchesQuery && matchesStatus && matchesDate
        }
    }
    val groups = remember(visibleTasks, nowMillis, sortMode) {
        groupTasks(visibleTasks, nowMillis, sortMode)
    }
    val filtersActive = searchQuery.isNotBlank() ||
        statusFilter != STATUS_ALL ||
        dateFilterKey != null
    val selectedTasks = tasks.filter { it.id in selectedIds }

    val taskAddedMessage = stringResource(R.string.task_added)
    val taskUpdatedMessage = stringResource(R.string.task_updated)
    val taskDeletedMessage = stringResource(R.string.task_deleted)
    val deletedWithNameFormat = stringResource(R.string.task_deleted_with_name)
    val undoLabel = stringResource(R.string.undo)
    val addTaskLabel = stringResource(R.string.add_task)
    val tasksDeletedMessage: (Int) -> String = {
        context.getString(R.string.tasks_deleted_count, it)
    }
    val tasksCompletedMessage: (Int) -> String = {
        context.getString(R.string.tasks_completed_count, it)
    }

    val deleteWithUndo: (TaskEntity) -> Unit = { task ->
        viewModel.deleteTask(task)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = deletedWithNameFormat.format(task.title),
                actionLabel = undoLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreTask(task)
            }
        }
    }

    val completeTask: (TaskEntity) -> Unit = { task ->
        if (!task.isCompleted) {
            viewModel.setTaskCompleted(task, true)
        }
    }

    val enterSelection: (TaskEntity) -> Unit = { task ->
        selectionMode = true
        showCalendar = false
        selectedIds = setOf(task.id)
    }

    val toggleSelection: (TaskEntity) -> Unit = { task ->
        selectedIds = if (task.id in selectedIds) {
            selectedIds - task.id
        } else {
            selectedIds + task.id
        }
    }

    val exitSelection: () -> Unit = {
        selectionMode = false
        selectedIds = emptySet()
    }

    val batchDelete: () -> Unit = {
        val targets = selectedTasks
        if (targets.isNotEmpty()) {
            viewModel.deleteTasks(targets)
            exitSelection()
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = tasksDeletedMessage(targets.size),
                    actionLabel = undoLabel,
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.restoreTasks(targets)
                }
            }
        }
    }

    val batchComplete: () -> Unit = {
        if (selectedTasks.isNotEmpty()) {
            viewModel.setTasksCompleted(selectedTasks, true)
            exitSelection()
            showSnackbar(scope, snackbarHostState, tasksCompletedMessage(selectedTasks.size))
        }
    }

    val changeMonth: (Int) -> Unit = { delta ->
        val target = TaskDateUtils.addMonths(calendarMonth, delta)
        calendarMonth = target
        val currentMonthKey = TaskDateUtils.dayKey(nowMillis).substring(0, 7)
        selectedDateKey = if (TaskDateUtils.dayKey(target).substring(0, 7) == currentMonthKey) {
            TaskDateUtils.dayKey(nowMillis)
        } else {
            TaskDateUtils.dayKey(target)
        }
    }

    val goToToday: () -> Unit = {
        calendarMonth = TaskDateUtils.firstDayOfMonth(nowMillis)
        selectedDateKey = TaskDateUtils.dayKey(nowMillis)
    }

    val selectDay: (MonthCell) -> Unit = { cell ->
        if (TaskDateUtils.dayKey(calendarMonth).substring(0, 7) !=
            cell.dateKey.substring(0, 7)
        ) {
            calendarMonth = TaskDateUtils.firstDayOfMonth(cell.dayMillis)
        }
        selectedDateKey = cell.dateKey
    }

    val newTaskDueAt = if (showCalendar) {
        TaskDateUtils.atDateKeyWithTime(
            selectedDateKey,
            TaskDateUtils.hourOf(nowMillis),
            TaskDateUtils.minuteOf(nowMillis)
        )
    } else {
        dateFilterKey?.let { key ->
            TaskDateUtils.atDateKeyWithTime(
                key,
                TaskDateUtils.hourOf(nowMillis),
                TaskDateUtils.minuteOf(nowMillis)
            )
        } ?: nowMillis
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = {
                        editingTaskId = null
                        editorVisible = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = addTaskLabel
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectionMode) {
                SelectionBar(
                    selectedCount = selectedTasks.size,
                    onExit = exitSelection,
                    onComplete = batchComplete,
                    onDelete = batchDelete
                )
            } else {
                AppHeader(
                    todayDone = todayTasks.count { it.isCompleted },
                    todayTotal = todayTasks.size,
                    nowMillis = nowMillis,
                    showCalendar = showCalendar,
                    searchActive = searchActive,
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
                    sortMode = sortMode,
                    onToggleView = { showCalendar = it },
                    onSearchClick = {
                        if (searchActive) {
                            searchActive = false
                            searchQuery = ""
                        } else {
                            searchActive = true
                            showCalendar = false
                        }
                    },
                    onSearchQueryChange = { searchQuery = it },
                    onStatusFilterChange = { statusFilter = it },
                    onSortModeChange = { sortModeIndex = it.ordinal },
                    onStatsClick = { statsVisible = true },
                    onSettingsClick = { settingsVisible = true }
                )
            }

            AnimatedContent(
                targetState = showCalendar,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { it / 12 })
                        .togetherWith(fadeOut() + slideOutHorizontally { -it / 12 })
                },
                label = "mainViewTransition"
            ) { isCalendar ->
                if (isCalendar) {
                    CalendarContent(
                        monthMillis = calendarMonth,
                        selectedDateKey = selectedDateKey,
                        todayKey = todayKey,
                        tasksByDay = tasksByDay,
                        nowMillis = nowMillis,
                        onChangeMonth = changeMonth,
                        onGoToToday = goToToday,
                        onSelectDay = selectDay,
                        onAddTask = {
                            editingTaskId = null
                            editorVisible = true
                        },
                        onToggle = { viewModel.toggleTask(it) },
                        onEdit = { task ->
                            editingTaskId = task.id
                            editorVisible = true
                        },
                        onDelete = { taskPendingDeleteId = it.id },
                        onSwipeComplete = completeTask,
                        onSwipeDelete = deleteWithUndo
                    )
                } else if (tasks.isEmpty()) {
                    EmptyState(modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 2.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item(key = "week-strip") {
                            WeekStrip(
                                weekCells = weekCells,
                                dateFilterKey = dateFilterKey,
                                todayKey = todayKey,
                                tasksByDay = tasksByDay,
                                onSelectDate = { key ->
                                    dateFilterKey = if (dateFilterKey == key) null else key
                                },
                                onClearDate = { dateFilterKey = null }
                            )
                        }

                        if (groups.isEmpty() && filtersActive) {
                            item(key = "no-results") {
                                NoResultsCard(
                                    onClearFilters = {
                                        searchQuery = ""
                                        statusFilter = STATUS_ALL
                                        dateFilterKey = null
                                    }
                                )
                            }
                        }

                        groups.forEach { group ->
                            item(key = "group-${group.dateKey}") {
                                GroupHeader(group = group)
                            }
                            items(
                                items = group.tasks,
                                key = { task -> task.id }
                            ) { task ->
                                SwipeableTaskCard(
                                    modifier = Modifier.animateItem(),
                                    task = task,
                                    selectionMode = selectionMode,
                                    selected = task.id in selectedIds,
                                    onToggle = { viewModel.toggleTask(task) },
                                    onEdit = {
                                        editingTaskId = task.id
                                        editorVisible = true
                                    },
                                    onDelete = { taskPendingDeleteId = task.id },
                                    onSwipeComplete = { completeTask(task) },
                                    onSwipeDelete = { deleteWithUndo(task) },
                                    onSelectToggle = { toggleSelection(task) },
                                    onEnterSelection = { enterSelection(task) },
                                    onShowSelectionMenu = { enterSelection(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (editorVisible) {
        TaskEditorDialog(
            initialTask = editingTask,
            defaultDueAt = newTaskDueAt,
            onDismiss = {
                editorVisible = false
                editingTaskId = null
            },
            onSave = { title, dueAt, colorIndex, note, priority, reminderEnabled, repeatRule ->
                val existingTask = editingTask
                if (existingTask == null) {
                    viewModel.addTask(
                        title = title,
                        dueAt = dueAt,
                        colorIndex = colorIndex,
                        note = note,
                        priority = priority,
                        reminderEnabled = reminderEnabled,
                        repeatRule = repeatRule
                    )
                    showSnackbar(scope, snackbarHostState, taskAddedMessage)
                } else {
                    viewModel.updateTask(
                        task = existingTask,
                        title = title,
                        dueAt = dueAt,
                        colorIndex = colorIndex,
                        note = note,
                        priority = priority,
                        reminderEnabled = reminderEnabled,
                        repeatRule = repeatRule
                    )
                    showSnackbar(scope, snackbarHostState, taskUpdatedMessage)
                }
                if (showCalendar) {
                    selectedDateKey = TaskDateUtils.dayKey(dueAt)
                    calendarMonth = TaskDateUtils.firstDayOfMonth(dueAt)
                }
                editorVisible = false
                editingTaskId = null
            }
        )
    }

    if (settingsVisible) {
        SettingsDialog(
            themeMode = themeMode,
            accentKey = accentKey,
            onThemeModeChange = onThemeModeChange,
            onAccentChange = onAccentChange,
            onDismiss = { settingsVisible = false }
        )
    }

    if (statsVisible) {
        StatsDialog(
            tasks = tasks,
            nowMillis = nowMillis,
            onDismiss = { statsVisible = false }
        )
    }

    if (taskPendingDelete != null) {
        AlertDialog(
            onDismissRequest = { taskPendingDeleteId = null },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.delete_confirm_message,
                        taskPendingDelete.title
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask(taskPendingDelete)
                        taskPendingDeleteId = null
                        showSnackbar(scope, snackbarHostState, taskDeletedMessage)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { taskPendingDeleteId = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AppHeader(
    todayDone: Int,
    todayTotal: Int,
    nowMillis: Long,
    showCalendar: Boolean,
    searchActive: Boolean,
    searchQuery: String,
    statusFilter: Int,
    sortMode: SortMode,
    onToggleView: (Boolean) -> Unit,
    onSearchClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (Int) -> Unit,
    onSortModeChange: (SortMode) -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val searchLabel = stringResource(R.string.search_tasks)
    val clearSearchLabel = stringResource(R.string.clear_search)
    val settingsLabel = stringResource(R.string.settings_title)
    val statsLabel = stringResource(R.string.stats_title)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = TaskDateUtils.formatToday(nowMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (searchActive) {
                        Icons.Rounded.Close
                    } else {
                        Icons.Rounded.Search
                    },
                    contentDescription = if (searchActive) clearSearchLabel else searchLabel,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onStatsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = statsLabel,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = settingsLabel,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            ViewToggle(
                showCalendar = showCalendar,
                onToggleView = onToggleView
            )
            Spacer(modifier = Modifier.weight(1f))
            if (todayTotal > 0) {
                TodayProgressChip(todayDone = todayDone, todayTotal = todayTotal)
            }
        }

        if (searchActive) {
            Spacer(modifier = Modifier.height(10.dp))
            SearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                clearLabel = clearSearchLabel
            )
        }

        if (!showCalendar) {
            Spacer(modifier = Modifier.height(10.dp))
            StatusFilterRow(
                selected = statusFilter,
                onSelect = onStatusFilterChange,
                sortMode = sortMode,
                onSortModeChange = onSortModeChange
            )
        }
    }
}

@Composable
private fun SelectionBar(
    selectedCount: Int,
    onExit: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val exitLabel = stringResource(R.string.exit_selection)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = exitLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = stringResource(R.string.selected_count, selectedCount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = onComplete,
                enabled = selectedCount > 0
            ) {
                Text(stringResource(R.string.batch_complete))
            }
            TextButton(
                onClick = onDelete,
                enabled = selectedCount > 0
            ) {
                Text(
                    text = stringResource(R.string.batch_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TodayProgressChip(todayDone: Int, todayTotal: Int) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.TaskAlt,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(
                    R.string.today_progress,
                    todayDone,
                    todayTotal
                ),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    clearLabel: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_search_field"),
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = clearLabel,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun StatusFilterRow(
    selected: Int,
    onSelect: (Int) -> Unit,
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selected == STATUS_ALL,
                onClick = { onSelect(STATUS_ALL) },
                label = {
                    Text(
                        text = stringResource(R.string.filter_all),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
            FilterChip(
                selected = selected == STATUS_UNDONE,
                onClick = { onSelect(STATUS_UNDONE) },
                label = {
                    Text(
                        text = stringResource(R.string.filter_undone),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
            FilterChip(
                selected = selected == STATUS_DONE,
                onClick = { onSelect(STATUS_DONE) },
                label = {
                    Text(
                        text = stringResource(R.string.filter_done),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        SortMenu(sortMode = sortMode, onSortModeChange = onSortModeChange)
    }
}

@Composable
private fun SortMenu(
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sortLabel = stringResource(R.string.sort_label)

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SwapVert,
                contentDescription = sortLabel,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(
                SortMode.TIME to R.string.sort_time,
                SortMode.PRIORITY to R.string.sort_priority,
                SortMode.CREATED to R.string.sort_created
            ).forEach { (mode, labelRes) ->
                DropdownMenuItem(
                    text = { Text(stringResource(labelRes)) },
                    leadingIcon = if (sortMode == mode) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        expanded = false
                        onSortModeChange(mode)
                    }
                )
            }
        }
    }
}

@Composable
private fun ViewToggle(
    showCalendar: Boolean,
    onToggleView: (Boolean) -> Unit
) {
    val listLabel = stringResource(R.string.view_list)
    val calendarLabel = stringResource(R.string.view_calendar)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ViewToggleButton(
                selected = !showCalendar,
                icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                label = listLabel,
                onClick = { onToggleView(false) }
            )
            ViewToggleButton(
                selected = showCalendar,
                icon = Icons.Rounded.CalendarMonth,
                label = calendarLabel,
                onClick = { onToggleView(true) }
            )
        }
    }
}

@Composable
private fun ViewToggleButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .semantics { contentDescription = label },
        shape = RoundedCornerShape(15.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun WeekStrip(
    weekCells: List<MonthCell>,
    dateFilterKey: String?,
    todayKey: String,
    tasksByDay: Map<String, List<TaskEntity>>,
    onSelectDate: (String) -> Unit,
    onClearDate: () -> Unit
) {
    val weekdays = stringArrayResource(R.array.weekday_short)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.week_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                if (dateFilterKey != null) {
                    TextButton(
                        onClick = onClearDate,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.show_all_tasks),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                weekCells.forEachIndexed { index, cell ->
                    val cellTasks = tasksByDay[cell.dateKey].orEmpty()
                    val isSelected = cell.dateKey == dateFilterKey
                    val isToday = cell.dateKey == todayKey
                    val description = stringResource(
                        R.string.week_day_desc,
                        cell.month,
                        cell.dayOfMonth,
                        cellTasks.size
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        Surface(
                            onClick = { onSelectDate(cell.dateKey) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .semantics { contentDescription = description },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = weekdays[index % 7],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cell.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isToday) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.height(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    cellTasks.take(3).forEach { task ->
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(taskColor(task.colorIndex), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoResultsCard(onClearFilters: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.no_results_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.no_results_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onClearFilters) {
                Text(stringResource(R.string.clear_filters))
            }
        }
    }
}

@Composable
private fun CalendarContent(
    monthMillis: Long,
    selectedDateKey: String,
    todayKey: String,
    tasksByDay: Map<String, List<TaskEntity>>,
    nowMillis: Long,
    onChangeMonth: (Int) -> Unit,
    onGoToToday: () -> Unit,
    onSelectDay: (MonthCell) -> Unit,
    onAddTask: () -> Unit,
    onToggle: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onSwipeComplete: (TaskEntity) -> Unit,
    onSwipeDelete: (TaskEntity) -> Unit
) {
    val selectedTasks = tasksByDay[selectedDateKey].orEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 2.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "calendar-card") {
            CalendarCard(
                monthMillis = monthMillis,
                selectedDateKey = selectedDateKey,
                todayKey = todayKey,
                tasksByDay = tasksByDay,
                onChangeMonth = onChangeMonth,
                onGoToToday = onGoToToday,
                onSelectDay = onSelectDay
            )
        }
        item(key = "selected-day-header") {
            SelectedDayHeader(
                selectedDateKey = selectedDateKey,
                nowMillis = nowMillis,
                taskCount = selectedTasks.size,
                onAddTask = onAddTask
            )
        }
        if (selectedTasks.isEmpty()) {
            item(key = "calendar-empty") {
                CalendarEmptyCard(onAddTask = onAddTask)
            }
        } else {
            items(
                items = selectedTasks,
                key = { task -> task.id }
            ) { task ->
                SwipeableTaskCard(
                    task = task,
                    selectionMode = false,
                    selected = false,
                    onToggle = { onToggle(task) },
                    onEdit = { onEdit(task) },
                    onDelete = { onDelete(task) },
                    onSwipeComplete = { onSwipeComplete(task) },
                    onSwipeDelete = { onSwipeDelete(task) },
                    onSelectToggle = { onToggle(task) },
                    onEnterSelection = {},
                    onShowSelectionMenu = {},
                    selectionActionsEnabled = false
                )
            }
        }
    }
}

@Composable
private fun CalendarCard(
    monthMillis: Long,
    selectedDateKey: String,
    todayKey: String,
    tasksByDay: Map<String, List<TaskEntity>>,
    onChangeMonth: (Int) -> Unit,
    onGoToToday: () -> Unit,
    onSelectDay: (MonthCell) -> Unit
) {
    val cells = remember(monthMillis) { TaskDateUtils.buildMonthCells(monthMillis) }
    val weekdays = stringArrayResource(R.array.weekday_short)
    val prevLabel = stringResource(R.string.calendar_prev_month)
    val nextLabel = stringResource(R.string.calendar_next_month)
    val todayLabel = stringResource(R.string.back_to_today)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onChangeMonth(-1) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = prevLabel,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = TaskDateUtils.formatMonth(monthMillis),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                TextButton(
                    onClick = onGoToToday,
                    modifier = Modifier.semantics { contentDescription = todayLabel },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = todayLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { onChangeMonth(1) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = nextLabel,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { weekday ->
                    Text(
                        text = weekday,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { cell ->
                        DayCell(
                            modifier = Modifier.weight(1f),
                            cell = cell,
                            tasks = tasksByDay[cell.dateKey].orEmpty(),
                            isSelected = cell.dateKey == selectedDateKey,
                            isToday = cell.dateKey == todayKey,
                            onClick = onSelectDay
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    cell: MonthCell,
    tasks: List<TaskEntity>,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: (MonthCell) -> Unit
) {
    val description = stringResource(
        R.string.calendar_day_desc,
        cell.year,
        cell.month,
        cell.dayOfMonth,
        tasks.size
    )

    Box(modifier = modifier.padding(2.dp)) {
        Surface(
            onClick = { onClick(cell) },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .semantics { contentDescription = description },
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            },
            border = if (isToday && !isSelected) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
            } else {
                null
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = cell.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday || isSelected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        !cell.isCurrentMonth -> MaterialTheme.colorScheme
                            .onSurfaceVariant.copy(alpha = 0.4f)
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.height(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    tasks.take(3).forEach { task ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(taskColor(task.colorIndex), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDayHeader(
    selectedDateKey: String,
    nowMillis: Long,
    taskCount: Int,
    onAddTask: () -> Unit
) {
    val selectedMillis = remember(selectedDateKey) {
        TaskDateUtils.atDateKeyWithTime(selectedDateKey, 0, 0)
    }
    val title = when (dateRelation(selectedDateKey, nowMillis)) {
        DayRelation.TODAY -> stringResource(R.string.today)
        DayRelation.TOMORROW -> stringResource(R.string.tomorrow)
        DayRelation.YESTERDAY -> stringResource(R.string.yesterday)
        DayRelation.OTHER -> TaskDateUtils.formatDayWithWeekday(selectedMillis)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(3.dp)
                )
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.calendar_task_count, taskCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            onClick = onAddTask,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.add_task_on_date),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CalendarEmptyCard(onAddTask: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.TaskAlt,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.calendar_empty_date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.calendar_empty_date_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAddTask) {
                Text(stringResource(R.string.add_task_on_date))
            }
        }
    }
}

@Composable
private fun GroupHeader(group: TaskGroup) {
    val title = when (group.relation) {
        DayRelation.TODAY -> stringResource(R.string.today)
        DayRelation.TOMORROW -> stringResource(R.string.tomorrow)
        DayRelation.YESTERDAY -> stringResource(R.string.yesterday)
        DayRelation.OTHER -> group.dateKey
    }
    val allCompleted = group.tasks.all { it.isCompleted }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(3.dp)
                )
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.group_task_count, group.tasks.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (allCompleted) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskCard(
    modifier: Modifier = Modifier,
    task: TaskEntity,
    selectionMode: Boolean,
    selected: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSwipeComplete: () -> Unit,
    onSwipeDelete: () -> Unit,
    onSelectToggle: () -> Unit,
    onEnterSelection: () -> Unit,
    onShowSelectionMenu: () -> Unit,
    selectionActionsEnabled: Boolean = true
) {
    if (selectionMode) {
        TaskCard(
            modifier = modifier,
            task = task,
            selectionMode = true,
            selected = selected,
            onToggle = onSelectToggle,
            onLongPress = onSelectToggle,
            onEdit = onEdit,
            onDelete = onDelete,
            onShowSelectionMenu = onShowSelectionMenu,
            selectionActionsEnabled = false
        )
        return
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeComplete()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeDelete()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { distance -> distance * 0.35f }
    )

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            SwipeActionBackground(direction = dismissState.dismissDirection)
        },
        enableDismissFromStartToEnd = !task.isCompleted,
        enableDismissFromEndToStart = true
    ) {
        TaskCard(
            task = task,
            selectionMode = false,
            selected = false,
            onToggle = onToggle,
            onLongPress = onEnterSelection,
            onEdit = onEdit,
            onDelete = onDelete,
            onShowSelectionMenu = onShowSelectionMenu,
            selectionActionsEnabled = selectionActionsEnabled
        )
    }
}

@Composable
private fun SwipeActionBackground(direction: SwipeToDismissBoxValue) {
    if (direction == SwipeToDismissBoxValue.Settled) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val isComplete = direction == SwipeToDismissBoxValue.StartToEnd
    val containerColor = if (isComplete) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (isComplete) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isComplete) Arrangement.Start else Arrangement.End
    ) {
        if (isComplete) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.swipe_complete),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        } else {
            Text(
                text = stringResource(R.string.swipe_delete),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskCard(
    modifier: Modifier = Modifier,
    task: TaskEntity,
    selectionMode: Boolean,
    selected: Boolean,
    onToggle: () -> Unit,
    onLongPress: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowSelectionMenu: () -> Unit,
    selectionActionsEnabled: Boolean = true
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val completed = task.isCompleted
    val accentColor = taskColor(task.colorIndex)

    val containerColor by animateColorAsState(
        targetValue = if (completed) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "taskContainerColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (completed) {
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
        },
        label = "taskBorderColor"
    )
    val titleColor by animateColorAsState(
        targetValue = if (completed) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "taskTitleColor"
    )
    val checkBackground by animateColorAsState(
        targetValue = if (completed) {
            accentColor
        } else {
            Color.Transparent
        },
        label = "taskCheckBackground"
    )
    val checkBorder by animateColorAsState(
        targetValue = if (completed) {
            Color.Transparent
        } else {
            accentColor.copy(alpha = 0.55f)
        },
        label = "taskCheckBorder"
    )

    val completeLabel = stringResource(
        if (completed) R.string.mark_incomplete else R.string.mark_complete
    )
    val moreLabel = stringResource(R.string.more_actions)
    val selectionLabel = stringResource(
        if (selected) R.string.unselect_task else R.string.select_task
    )
    val repeatLabel = repeatRuleLabel(task.repeatRule)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = if (selectionMode) onToggle else onEdit,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = if (completed) 0.dp else 1.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(end = 2.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .width(4.dp)
                    .height(40.dp)
                    .background(
                        color = accentColor.copy(alpha = if (completed) 0.4f else 1f),
                        shape = RoundedCornerShape(3.dp)
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (selectionMode) {
                Surface(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = selectionLabel },
                    shape = CircleShape,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    border = BorderStroke(
                        1.5.dp,
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        }
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            } else {
                Surface(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = completeLabel },
                    shape = CircleShape,
                    color = checkBackground,
                    border = BorderStroke(1.5.dp, checkBorder)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completed) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = accentColor.copy(alpha = 0.55f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = titleColor,
                        textDecoration = if (completed) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (task.priority > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        PriorityBadge()
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (completed) 0.55f else 0.85f
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = TaskDateUtils.formatDateTime(task.dueAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (completed) 0.55f else 0.85f
                        )
                    )
                    if (task.repeatRule != RepeatRule.NONE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(
                                alpha = if (completed) 0.5f else 0.9f
                            )
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = repeatLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = if (completed) 0.5f else 0.9f
                            )
                        )
                    }
                }
                if (task.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Notes,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (completed) 0.55f else 0.8f
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (completed) 0.55f else 0.8f
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (!selectionMode) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = moreLabel,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (selectionActionsEnabled) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.selection_mode)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onShowSelectionMenu()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun repeatRuleLabel(rule: Int): String = when (rule) {
    RepeatRule.DAILY -> stringResource(R.string.repeat_daily)
    RepeatRule.WEEKLY -> stringResource(R.string.repeat_weekly)
    RepeatRule.WEEKDAYS -> stringResource(R.string.repeat_weekdays)
    else -> ""
}

@Composable
private fun PriorityBadge() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Flag,
                contentDescription = null,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = stringResource(R.string.priority_high),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.TaskAlt,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = stringResource(R.string.empty_state_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_state_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun showSnackbar(
    scope: kotlinx.coroutines.CoroutineScope,
    hostState: SnackbarHostState,
    message: String
) {
    scope.launch {
        hostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
    }
}

@Composable
private fun rememberCurrentTimeMillis(): Long {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            nowMillis = System.currentTimeMillis()
        }
    }
    return nowMillis
}

private const val STATUS_ALL = 0
private const val STATUS_UNDONE = 1
private const val STATUS_DONE = 2
