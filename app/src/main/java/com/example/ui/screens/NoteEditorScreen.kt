package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Note
import com.example.data.model.Notebook
import com.example.ui.NotebookViewModel
import com.example.ui.components.NoteColorPicker
import com.example.ui.components.formatDate
import com.example.ui.components.getNotebookIcon
import com.example.ui.components.parseHexColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long,
    initialIsChecklist: Boolean,
    viewModel: NotebookViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notebooks by viewModel.notebooks.collectAsStateWithLifecycle()

    var currentNoteId by remember { mutableLongStateOf(noteId) }
    var title by remember { mutableStateOf("") }
    var contentValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = if (initialIsChecklist && noteId == 0L) "- [ ] " else "",
                selection = TextRange(if (initialIsChecklist && noteId == 0L) 6 else 0)
            )
        )
    }
    var notebookId by remember { mutableStateOf<Long?>(null) }
    var colorHex by remember { mutableStateOf("#FFFDF8") }
    var isPinned by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var isChecklist by remember { mutableStateOf(initialIsChecklist) }
    var tags by remember { mutableStateOf("") }
    var lastUpdated by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showColorPicker by remember { mutableStateOf(false) }
    var showNotebookMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }

    val contentFocusRequester = remember { FocusRequester() }

    // Load initial note data if editing
    LaunchedEffect(noteId) {
        if (noteId != 0L) {
            val note = viewModel.getNote(noteId)
            if (note != null) {
                title = note.title
                contentValue = TextFieldValue(text = note.content, selection = TextRange(note.content.length))
                notebookId = note.notebookId
                colorHex = note.colorHex
                isPinned = note.isPinned
                isFavorite = note.isFavorite
                isChecklist = note.isChecklist
                tags = note.tags
                lastUpdated = note.updatedAt
            }
        }
        isLoaded = true
    }

    // Auto-save function
    fun saveCurrentNote() {
        val trimmedTitle = title.trim()
        val currentText = contentValue.text.trim()
        if (trimmedTitle.isNotBlank() || currentText.isNotBlank()) {
            viewModel.saveNote(
                id = currentNoteId,
                title = trimmedTitle,
                content = contentValue.text,
                notebookId = notebookId,
                colorHex = colorHex,
                isPinned = isPinned,
                isFavorite = isFavorite,
                isChecklist = isChecklist,
                tags = tags
            ) { savedId ->
                currentNoteId = savedId
            }
        }
    }

    // Intercept back button to persist changes
    BackHandler {
        saveCurrentNote()
        onNavigateBack()
    }

    val paperBackground = parseHexColor(colorHex, MaterialTheme.colorScheme.background)
    val wordCount = remember(contentValue.text) {
        if (contentValue.text.isBlank()) 0
        else contentValue.text.trim().split("\\s+".toRegex()).size
    }
    val charCount = remember(contentValue.text) { contentValue.text.length }

    val currentNotebook = remember(notebookId, notebooks) {
        notebooks.firstOrNull { it.id == notebookId }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = paperBackground,
        topBar = {
            TopAppBar(
                title = {
                    // Notebook selector pill
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (currentNotebook != null) {
                                parseHexColor(currentNotebook.colorHex).copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                1.dp,
                                if (currentNotebook != null) {
                                    parseHexColor(currentNotebook.colorHex).copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showNotebookMenu = true }
                                .testTag("notebook_selector_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentNotebook != null) {
                                        getNotebookIcon(currentNotebook.iconName)
                                    } else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (currentNotebook != null) {
                                        parseHexColor(currentNotebook.colorHex)
                                    } else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = currentNotebook?.name ?: "Unfiled",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (currentNotebook != null) {
                                        parseHexColor(currentNotebook.colorHex)
                                    } else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Notebook selection dropdown
                        DropdownMenu(
                            expanded = showNotebookMenu,
                            onDismissRequest = { showNotebookMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Unfiled") },
                                onClick = {
                                    notebookId = null
                                    showNotebookMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Folder, contentDescription = null)
                                }
                            )
                            notebooks.forEach { nb ->
                                val nbColor = parseHexColor(nb.colorHex)
                                DropdownMenuItem(
                                    text = { Text(nb.name) },
                                    onClick = {
                                        notebookId = nb.id
                                        showNotebookMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getNotebookIcon(nb.iconName),
                                            contentDescription = null,
                                            tint = nbColor
                                        )
                                    }
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            saveCurrentNote()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Save and go back"
                        )
                    }
                },
                actions = {
                    // Pin toggle
                    IconButton(
                        onClick = { isPinned = !isPinned },
                        modifier = Modifier.testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "Unpin" else "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Favorite toggle
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier.testTag("editor_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (isFavorite) "Starred" else "Star",
                            tint = if (isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Color Palette
                    IconButton(
                        onClick = { showColorPicker = !showColorPicker },
                        modifier = Modifier.testTag("editor_color_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Paper Color",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                if (title.isNotBlank()) append(title).append("\n\n")
                                append(contentValue.text)
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Note"))
                        },
                        modifier = Modifier.testTag("editor_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share note"
                        )
                    }

                    // Delete button
                    if (currentNoteId != 0L) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.testTag("editor_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete note",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = paperBackground
                )
            )
        },
        bottomBar = {
            // Formatting & Quick Insertion Toolbar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = paperBackground,
                tonalElevation = 3.dp,
                border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Checklist item toggle
                    ToolbarButton(
                        icon = Icons.Default.CheckBox,
                        label = "Checklist",
                        isSelected = isChecklist,
                        onClick = {
                            isChecklist = !isChecklist
                            val cur = contentValue.text
                            val newText = if (cur.isBlank()) "- [ ] " else "$cur\n- [ ] "
                            contentValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                        }
                    )

                    // Bullet list
                    ToolbarButton(
                        icon = Icons.Default.FormatListBulleted,
                        label = "Bullet",
                        isSelected = false,
                        onClick = {
                            val cur = contentValue.text
                            val newText = if (cur.isBlank()) "• " else "$cur\n• "
                            contentValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                        }
                    )

                    // Numbered list
                    ToolbarButton(
                        icon = Icons.Default.FormatListNumbered,
                        label = "Number",
                        isSelected = false,
                        onClick = {
                            val cur = contentValue.text
                            val newText = if (cur.isBlank()) "1. " else "$cur\n1. "
                            contentValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                        }
                    )

                    // Header
                    ToolbarButton(
                        icon = Icons.Default.Title,
                        label = "Header",
                        isSelected = false,
                        onClick = {
                            val cur = contentValue.text
                            val newText = if (cur.isBlank()) "## " else "$cur\n## "
                            contentValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                        }
                    )

                    // Bold
                    ToolbarButton(
                        icon = Icons.Default.FormatBold,
                        label = "Bold",
                        isSelected = false,
                        onClick = {
                            val sel = contentValue.selection
                            val full = contentValue.text
                            if (sel.start != sel.end) {
                                val selectedSub = full.substring(sel.start, sel.end)
                                val newText = full.replaceRange(sel.start, sel.end, "**$selectedSub**")
                                contentValue = TextFieldValue(newText, TextRange(sel.start + 2 + selectedSub.length + 2))
                            } else {
                                val newText = full.substring(0, sel.start) + "****" + full.substring(sel.start)
                                contentValue = TextFieldValue(newText, TextRange(sel.start + 2))
                            }
                        }
                    )

                    // Insert timestamp
                    ToolbarButton(
                        icon = Icons.Default.AccessTime,
                        label = "Time",
                        isSelected = false,
                        onClick = {
                            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                            val stamp = "[${sdf.format(Date())}] "
                            val cur = contentValue.text
                            val newText = if (cur.isBlank()) stamp else "$cur\n$stamp"
                            contentValue = TextFieldValue(newText, TextRange(newText.length))
                        }
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
            // Expandable Color Picker
            AnimatedVisibility(
                visible = showColorPicker,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    color = paperBackground,
                    border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.06f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NoteColorPicker(
                        selectedColor = colorHex,
                        onColorSelected = { colorHex = it },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Scrollable Writing Canvas
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Title Field
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            text = "Title",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF1E1A17).copy(alpha = 0.35f)
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF1E1A17)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { contentFocusRequester.requestFocus() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                // Metadata Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Edited ${formatDate(lastUpdated)}",
                        fontSize = 11.5.sp,
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                    Text(
                        text = "$wordCount words • $charCount chars",
                        fontSize = 11.5.sp,
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                }

                // Tags Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = "Tags",
                        tint = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextField(
                        value = tags,
                        onValueChange = { tags = it },
                        placeholder = {
                            Text(
                                "Add tags (e.g., #meeting, #ideas)",
                                fontSize = 12.sp,
                                color = Color.Black.copy(alpha = 0.35f)
                            )
                        },
                        textStyle = TextStyle(fontSize = 12.sp, color = Color.Black.copy(alpha = 0.6f)),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_tags_input")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Note Body / Content Field
                TextField(
                    value = contentValue,
                    onValueChange = { contentValue = it },
                    placeholder = {
                        Text(
                            text = if (isChecklist) "Tap + Checklist or start typing items..." else "Start writing your note here...",
                            fontSize = 16.sp,
                            color = Color(0xFF423B36).copy(alpha = 0.4f),
                            lineHeight = 24.sp
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 25.sp,
                        color = Color(0xFF2C2520)
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(contentFocusRequester)
                        .testTag("note_content_input")
                )

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Note?") },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteNoteById(currentNoteId)
                        onNavigateBack()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.12f)),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
