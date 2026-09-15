package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Note
import com.example.data.model.Notebook
import com.example.ui.NoteFilter
import com.example.ui.NotebookViewModel
import com.example.ui.SortOption
import com.example.ui.ViewLayout
import com.example.ui.components.CreateNotebookDialog
import com.example.ui.components.NoteItemCard
import com.example.ui.components.getNotebookIcon
import com.example.ui.components.parseHexColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: NotebookViewModel,
    onNavigateToEditor: (noteId: Long, isChecklist: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val notebooks by viewModel.notebooks.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val viewLayout by viewModel.viewLayout.collectAsStateWithLifecycle()
    val allNotesCount by viewModel.allNotesCount.collectAsStateWithLifecycle()
    val favCount by viewModel.favoriteNotesCount.collectAsStateWithLifecycle()

    var isSearchActive by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreateNotebookDialog by remember { mutableStateOf(false) }
    var notebookToEdit by remember { mutableStateOf<Notebook?>(null) }

    val notebookMap = remember(notebooks) { notebooks.associateBy { it.id } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search notes, tags, checklists...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Notebook",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) viewModel.setSearchQuery("")
                        },
                        modifier = Modifier.testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "Close search" else "Search"
                        )
                    }

                    // Sort menu button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort notes"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sortOption == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // View Layout toggle
                    IconButton(
                        onClick = { viewModel.toggleViewLayout() },
                        modifier = Modifier.testTag("toggle_layout_button")
                    ) {
                        Icon(
                            imageVector = if (viewLayout == ViewLayout.GRID) Icons.Default.ViewAgenda else Icons.Default.GridView,
                            contentDescription = "Switch layout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary quick checklist button
                SmallFloatingActionButton(
                    onClick = { onNavigateToEditor(0L, true) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.testTag("new_checklist_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckBox,
                        contentDescription = "New checklist note",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Primary New Note FAB
                ExtendedFloatingActionButton(
                    onClick = { onNavigateToEditor(0L, false) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = "New Note",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier.testTag("new_note_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Notebooks Shelf Filter Row
            NotebooksShelfRow(
                notebooks = notebooks,
                selectedFilter = selectedFilter,
                allNotesCount = allNotesCount,
                favoriteNotesCount = favCount,
                onSelectFilter = { viewModel.setFilter(it) },
                onAddNewNotebook = { showCreateNotebookDialog = true },
                onEditNotebook = { notebookToEdit = it }
            )

            // Main Content: Notes
            if (notes.isEmpty()) {
                EmptyNotesView(
                    searchQuery = searchQuery,
                    selectedFilter = selectedFilter,
                    notebookMap = notebookMap,
                    onCreateNote = { onNavigateToEditor(0L, false) }
                )
            } else {
                val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
                val otherNotes = remember(notes) { notes.filter { !it.isPinned } }

                if (viewLayout == ViewLayout.GRID) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notes_staggered_grid"),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalItemSpacing = 10.dp
                    ) {
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                SectionHeader(title = "PINNED", count = pinnedNotes.size, icon = Icons.Default.PushPin)
                            }
                            items(pinnedNotes, key = { it.id }) { note ->
                                NoteItemCard(
                                    note = note,
                                    notebook = note.notebookId?.let { notebookMap[it] },
                                    onClick = { onNavigateToEditor(note.id, note.isChecklist) },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onToggleChecklistItem = { lineIndex ->
                                        viewModel.toggleChecklistItem(note, lineIndex)
                                    },
                                    onDelete = { viewModel.deleteNote(note) }
                                )
                            }
                        }

                        if (otherNotes.isNotEmpty()) {
                            if (pinnedNotes.isNotEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    SectionHeader(title = "NOTES", count = otherNotes.size, icon = null)
                                }
                            }
                            items(otherNotes, key = { it.id }) { note ->
                                NoteItemCard(
                                    note = note,
                                    notebook = note.notebookId?.let { notebookMap[it] },
                                    onClick = { onNavigateToEditor(note.id, note.isChecklist) },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onToggleChecklistItem = { lineIndex ->
                                        viewModel.toggleChecklistItem(note, lineIndex)
                                    },
                                    onDelete = { viewModel.deleteNote(note) }
                                )
                            }
                        }
                    }
                } else {
                    // Single-column List Layout
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notes_lazy_column"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                SectionHeader(title = "PINNED", count = pinnedNotes.size, icon = Icons.Default.PushPin)
                            }
                            items(pinnedNotes, key = { it.id }) { note ->
                                NoteItemCard(
                                    note = note,
                                    notebook = note.notebookId?.let { notebookMap[it] },
                                    onClick = { onNavigateToEditor(note.id, note.isChecklist) },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onToggleChecklistItem = { lineIndex ->
                                        viewModel.toggleChecklistItem(note, lineIndex)
                                    },
                                    onDelete = { viewModel.deleteNote(note) }
                                )
                            }
                        }

                        if (otherNotes.isNotEmpty()) {
                            if (pinnedNotes.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "NOTES", count = otherNotes.size, icon = null)
                                }
                            }
                            items(otherNotes, key = { it.id }) { note ->
                                NoteItemCard(
                                    note = note,
                                    notebook = note.notebookId?.let { notebookMap[it] },
                                    onClick = { onNavigateToEditor(note.id, note.isChecklist) },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onToggleChecklistItem = { lineIndex ->
                                        viewModel.toggleChecklistItem(note, lineIndex)
                                    },
                                    onDelete = { viewModel.deleteNote(note) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Notebook Dialog
    if (showCreateNotebookDialog) {
        CreateNotebookDialog(
            onDismiss = { showCreateNotebookDialog = false },
            onSave = { name, colorHex, iconName ->
                viewModel.createNotebook(name, colorHex, iconName)
                showCreateNotebookDialog = false
            }
        )
    }

    // Edit Notebook Dialog
    notebookToEdit?.let { nb ->
        CreateNotebookDialog(
            notebookToEdit = nb,
            onDismiss = { notebookToEdit = null },
            onSave = { name, colorHex, iconName ->
                viewModel.updateNotebook(nb.copy(name = name, colorHex = colorHex, iconName = iconName))
                notebookToEdit = null
            },
            onDelete = {
                viewModel.deleteNotebook(nb)
                notebookToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotebooksShelfRow(
    notebooks: List<Notebook>,
    selectedFilter: NoteFilter,
    allNotesCount: Int,
    favoriteNotesCount: Int,
    onSelectFilter: (NoteFilter) -> Unit,
    onAddNewNotebook: () -> Unit,
    onEditNotebook: (Notebook) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All Notes" chip
        item {
            val isSelected = selectedFilter is NoteFilter.All
            NotebookChip(
                label = "All Notes",
                count = allNotesCount,
                isSelected = isSelected,
                icon = Icons.Default.Book,
                tintColor = MaterialTheme.colorScheme.primary,
                onClick = { onSelectFilter(NoteFilter.All) },
                onLongClick = {}
            )
        }

        // "Favorites" chip
        item {
            val isSelected = selectedFilter is NoteFilter.Favorites
            NotebookChip(
                label = "Favorites",
                count = favoriteNotesCount,
                isSelected = isSelected,
                icon = Icons.Default.Star,
                tintColor = Color(0xFFF59E0B),
                onClick = { onSelectFilter(NoteFilter.Favorites) },
                onLongClick = {}
            )
        }

        // Notebooks
        items(notebooks, key = { it.id }) { notebook ->
            val isSelected = selectedFilter is NoteFilter.ByNotebook && selectedFilter.notebookId == notebook.id
            val color = parseHexColor(notebook.colorHex, MaterialTheme.colorScheme.primary)
            val icon = getNotebookIcon(notebook.iconName)

            NotebookChip(
                label = notebook.name,
                count = null,
                isSelected = isSelected,
                icon = icon,
                tintColor = color,
                onClick = { onSelectFilter(NoteFilter.ByNotebook(notebook.id)) },
                onLongClick = { onEditNotebook(notebook) }
            )
        }

        // Add Notebook button
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onAddNewNotebook)
                    .testTag("add_notebook_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Notebook",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Notebook",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotebookChip(
    label: String,
    count: Int?,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tintColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = if (isSelected) tintColor else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) tintColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else tintColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
            if (count != null && count > 0) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
        }
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Text(
            text = "($count)",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun EmptyNotesView(
    searchQuery: String,
    selectedFilter: NoteFilter,
    notebookMap: Map<Long, Notebook>,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtitle = when {
        searchQuery.isNotEmpty() -> "No notes found matching \"$searchQuery\""
        selectedFilter is NoteFilter.Favorites -> "No favorite notes yet.\nStar notes to find them quickly here."
        selectedFilter is NoteFilter.ByNotebook -> {
            val name = notebookMap[selectedFilter.notebookId]?.name ?: "this notebook"
            "No notes in \"$name\" yet.\nTap + to write the first note here."
        }
        else -> "Your notebook is waiting for thoughts, lists, and sketches."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Create,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = if (searchQuery.isNotEmpty()) "No Results Found" else "Empty Notebook",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            if (searchQuery.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = onCreateNote)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Write Note",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
