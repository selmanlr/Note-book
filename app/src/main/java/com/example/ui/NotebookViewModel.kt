package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Note
import com.example.data.model.Notebook
import com.example.data.repository.NotebookRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NoteFilter {
    data object All : NoteFilter
    data object Favorites : NoteFilter
    data object Unfiled : NoteFilter
    data class ByNotebook(val notebookId: Long) : NoteFilter
}

enum class SortOption(val label: String) {
    MODIFIED("Last Modified"),
    CREATED("Date Created"),
    ALPHABETICAL("Alphabetical (A-Z)")
}

enum class ViewLayout {
    GRID,
    LIST
}

class NotebookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotebookRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = NotebookRepository(database.notebookDao(), database.noteDao())
    }

    val notebooks: StateFlow<List<Notebook>> = repository.allNotebooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedFilter = MutableStateFlow<NoteFilter>(NoteFilter.All)
    val selectedFilter: StateFlow<NoteFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.MODIFIED)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _viewLayout = MutableStateFlow(ViewLayout.GRID)
    val viewLayout: StateFlow<ViewLayout> = _viewLayout.asStateFlow()

    // Notes filtered by notebook or favorites or unfiled
    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawNotes: StateFlow<List<Note>> = _selectedFilter.flatMapLatest { filter ->
        when (filter) {
            is NoteFilter.All -> repository.allNotes
            is NoteFilter.Favorites -> repository.favoriteNotes
            is NoteFilter.Unfiled -> repository.unfiledNotes
            is NoteFilter.ByNotebook -> repository.getNotesByNotebook(filter.notebookId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered & Sorted notes for the UI
    val notes: StateFlow<List<Note>> = combine(
        rawNotes,
        _searchQuery,
        _sortOption
    ) { noteList, query, sort ->
        var list = noteList
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { note ->
                note.title.lowercase().contains(q) ||
                note.content.lowercase().contains(q) ||
                note.tags.lowercase().contains(q)
            }
        }

        when (sort) {
            SortOption.MODIFIED -> list.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
            )
            SortOption.CREATED -> list.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.createdAt }
            )
            SortOption.ALPHABETICAL -> list.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase() }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Note count mapping for badges
    val allNotesCount: StateFlow<Int> = repository.allNotes
        .combine(MutableStateFlow(Unit)) { notes, _ -> notes.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val favoriteNotesCount: StateFlow<Int> = repository.favoriteNotes
        .combine(MutableStateFlow(Unit)) { notes, _ -> notes.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setFilter(filter: NoteFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleViewLayout() {
        _viewLayout.value = if (_viewLayout.value == ViewLayout.GRID) ViewLayout.LIST else ViewLayout.GRID
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    isPinned = !note.isPinned,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    isFavorite = !note.isFavorite,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }

    fun toggleChecklistItem(note: Note, lineIndex: Int) {
        val lines = note.content.split("\n").toMutableList()
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            if (line.startsWith("- [x] ")) {
                lines[lineIndex] = "- [ ] " + line.removePrefix("- [x] ")
            } else if (line.startsWith("- [ ] ")) {
                lines[lineIndex] = "- [x] " + line.removePrefix("- [ ] ")
            }
            val updatedContent = lines.joinToString("\n")
            viewModelScope.launch {
                repository.updateNote(
                    note.copy(
                        content = updatedContent,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun getNote(id: Long): Note? {
        return repository.getNoteByIdOnce(id)
    }

    fun saveNote(
        id: Long,
        title: String,
        content: String,
        notebookId: Long?,
        colorHex: String,
        isPinned: Boolean,
        isFavorite: Boolean,
        isChecklist: Boolean,
        tags: String,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (id == 0L) {
                val newId = repository.insertNote(
                    Note(
                        notebookId = notebookId,
                        title = title.trim(),
                        content = content,
                        isPinned = isPinned,
                        isFavorite = isFavorite,
                        colorHex = colorHex,
                        isChecklist = isChecklist,
                        tags = tags.trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                )
                onComplete(newId)
            } else {
                repository.updateNote(
                    Note(
                        id = id,
                        notebookId = notebookId,
                        title = title.trim(),
                        content = content,
                        isPinned = isPinned,
                        isFavorite = isFavorite,
                        colorHex = colorHex,
                        isChecklist = isChecklist,
                        tags = tags.trim(),
                        createdAt = getNote(id)?.createdAt ?: now,
                        updatedAt = now
                    )
                )
                onComplete(id)
            }
        }
    }

    fun createNotebook(name: String, colorHex: String, iconName: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertNotebook(
                Notebook(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    fun updateNotebook(notebook: Notebook) {
        viewModelScope.launch {
            repository.updateNotebook(notebook)
        }
    }

    fun deleteNotebook(notebook: Notebook) {
        viewModelScope.launch {
            if (_selectedFilter.value is NoteFilter.ByNotebook &&
                (_selectedFilter.value as NoteFilter.ByNotebook).notebookId == notebook.id
            ) {
                _selectedFilter.value = NoteFilter.All
            }
            repository.deleteNotebook(notebook)
        }
    }
}
