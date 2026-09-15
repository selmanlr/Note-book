package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.local.NotebookDao
import com.example.data.model.Note
import com.example.data.model.Notebook
import kotlinx.coroutines.flow.Flow

class NotebookRepository(
    private val notebookDao: NotebookDao,
    private val noteDao: NoteDao
) {
    val allNotebooks: Flow<List<Notebook>> = notebookDao.getAllNotebooks()
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    val favoriteNotes: Flow<List<Note>> = noteDao.getFavoriteNotes()
    val unfiledNotes: Flow<List<Note>> = noteDao.getNotesUnfiled()

    fun getNotebookById(id: Long): Flow<Notebook?> = notebookDao.getNotebookById(id)

    fun getNotesByNotebook(notebookId: Long): Flow<List<Note>> =
        noteDao.getNotesByNotebook(notebookId)

    fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query)

    fun getNoteById(id: Long): Flow<Note?> =
        noteDao.getNoteById(id)

    suspend fun getNoteByIdOnce(id: Long): Note? =
        noteDao.getNoteByIdOnce(id)

    suspend fun insertNote(note: Note): Long =
        noteDao.insertNote(note)

    suspend fun updateNote(note: Note) =
        noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) =
        noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) =
        noteDao.deleteNoteById(id)

    suspend fun insertNotebook(notebook: Notebook): Long =
        notebookDao.insertNotebook(notebook)

    suspend fun updateNotebook(notebook: Notebook) =
        notebookDao.updateNotebook(notebook)

    suspend fun deleteNotebook(notebook: Notebook) {
        // Unfile notes associated with this notebook so they are not deleted, then delete the notebook
        noteDao.unfileNotesFromNotebook(notebook.id)
        notebookDao.deleteNotebook(notebook)
    }
}
