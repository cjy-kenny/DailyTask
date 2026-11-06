package com.example.dailytasks.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dailytasks.R
import com.example.dailytasks.data.RepeatRule
import com.example.dailytasks.data.TaskEntity
import com.example.dailytasks.ui.theme.TaskColorOptions
import com.example.dailytasks.util.TaskDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialog(
    initialTask: TaskEntity?,
    defaultDueAt: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        dueAt: Long,
        colorIndex: Int,
        note: String,
        priority: Int,
        reminderEnabled: Boolean,
        repeatRule: Int
    ) -> Unit
) {
    val isEditing = initialTask != null
    var title by rememberSaveable(initialTask?.id) {
        mutableStateOf(initialTask?.title.orEmpty())
    }
    var note by rememberSaveable(initialTask?.id) {
        mutableStateOf(initialTask?.note.orEmpty())
    }
    var colorIndex by rememberSaveable(initialTask?.id) {
        mutableIntStateOf(initialTask?.colorIndex ?: 0)
    }
    var priority by rememberSaveable(initialTask?.id) {
        mutableIntStateOf(initialTask?.priority ?: 0)
    }
    var reminderEnabled by rememberSaveable(initialTask?.id) {
        mutableStateOf(initialTask?.reminderEnabled ?: true)
    }
    var repeatRule by rememberSaveable(initialTask?.id) {
        mutableIntStateOf(initialTask?.repeatRule ?: RepeatRule.NONE)
    }
    var dueAt by rememberSaveable(initialTask?.id) {
        mutableStateOf(initialTask?.dueAt ?: defaultDueAt)
    }
    var showError by rememberSaveable(initialTask?.id) { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val submit: () -> Unit = {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            showError = true
        } else {
            onSave(trimmedTitle, dueAt, colorIndex, note.trim(), priority, reminderEnabled, repeatRule)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (isEditing) R.string.edit_task else R.string.new_task
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            if (isEditing) {
                                R.string.edit_task_hint
                            } else {
                                R.string.new_task_hint
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { newValue ->
                            title = newValue
                            if (showError && newValue.isNotBlank()) {
                                showError = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_field"),
                        label = { Text(stringResource(R.string.task_title_label)) },
                        placeholder = {
                            Text(stringResource(R.string.task_title_placeholder))
                        },
                        singleLine = false,
                        minLines = 1,
                        maxLines = 3,
                        isError = showError,
                        supportingText = if (showError) {
                            { Text(stringResource(R.string.task_title_required)) }
                        } else {
                            null
                        },
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() })
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    TaskColorPicker(
                        selectedIndex = colorIndex,
                        onSelect = { colorIndex = it }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_note_field"),
                        label = { Text(stringResource(R.string.task_note_label)) },
                        placeholder = {
                            Text(stringResource(R.string.task_note_placeholder))
                        },
                        singleLine = false,
                        minLines = 1,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DateTimeSelector(
                            label = stringResource(R.string.date_label),
                            value = TaskDateUtils.formatDate(dueAt),
                            icon = Icons.Rounded.CalendarMonth,
                            onClick = {
                                focusManager.clearFocus()
                                showDatePicker = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                        DateTimeSelector(
                            label = stringResource(R.string.time_label),
                            value = TaskDateUtils.formatTime(dueAt),
                            icon = Icons.Rounded.Schedule,
                            onClick = {
                                focusManager.clearFocus()
                                showTimePicker = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrioritySelector(
                        selectedPriority = priority,
                        onSelect = { priority = it }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    RepeatSelector(
                        selectedRule = repeatRule,
                        onSelect = { repeatRule = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ReminderToggle(
                        enabled = reminderEnabled,
                        onToggle = { reminderEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = submit,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(
                                horizontal = 26.dp,
                                vertical = 12.dp
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.save),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = TaskDateUtils.toDatePickerMillis(dueAt)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis
                        if (selectedDate != null) {
                            dueAt = TaskDateUtils.combineDateAndTime(
                                datePickerMillis = selectedDate,
                                hour = TaskDateUtils.hourOf(dueAt),
                                minute = TaskDateUtils.minuteOf(dueAt)
                            )
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = TaskDateUtils.hourOf(dueAt),
            initialMinute = TaskDateUtils.minuteOf(dueAt),
            is24Hour = true
        )
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.select_time),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 20.dp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TimePicker(state = timePickerState)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showTimePicker = false },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = {
                                    dueAt = TaskDateUtils.combineDateAndTime(
                                        datePickerMillis = TaskDateUtils.toDatePickerMillis(dueAt),
                                        hour = timePickerState.hour,
                                        minute = timePickerState.minute
                                    )
                                    showTimePicker = false
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.confirm))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskColorPicker(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val rows = TaskColorOptions.chunked(4)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.task_color_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        rows.forEachIndexed { rowIndex, options ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEachIndexed { columnIndex, option ->
                    val index = rowIndex * 4 + columnIndex
                    val label = stringResource(option.labelRes)
                    val isSelected = selectedIndex == index
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { onSelect(index) }
                            .semantics {
                                contentDescription = label
                                selected = isSelected
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 32.dp else 28.dp)
                                .background(option.color, CircleShape)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
            if (rowIndex < rows.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PrioritySelector(
    selectedPriority: Int,
    onSelect: (Int) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Flag,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.priority_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedPriority == 0,
                onClick = { onSelect(0) },
                label = {
                    Text(
                        text = stringResource(R.string.priority_normal),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
            FilterChip(
                selected = selectedPriority == 1,
                onClick = { onSelect(1) },
                label = {
                    Text(
                        text = stringResource(R.string.priority_high),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun RepeatSelector(
    selectedRule: Int,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        RepeatRule.NONE to R.string.repeat_none,
        RepeatRule.DAILY to R.string.repeat_daily,
        RepeatRule.WEEKLY to R.string.repeat_weekly,
        RepeatRule.WEEKDAYS to R.string.repeat_weekdays
    )

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Repeat,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.repeat_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (rule, labelRes) ->
                FilterChip(
                    selected = selectedRule == rule,
                    onClick = { onSelect(rule) },
                    label = {
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ReminderToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.reminder_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.reminder_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun DateTimeSelector(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
