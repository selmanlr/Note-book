package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Note
import com.example.data.model.Notebook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Notebook::class, Note::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notebook_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database.notebookDao(), database.noteDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(notebookDao: NotebookDao, noteDao: NoteDao) {
            val workId = notebookDao.insertNotebook(
                Notebook(name = "Work & Projects", colorHex = "#1E88E5", iconName = "work")
            )
            val ideasId = notebookDao.insertNotebook(
                Notebook(name = "Creative Ideas", colorHex = "#8E24AA", iconName = "lightbulb")
            )
            val personalId = notebookDao.insertNotebook(
                Notebook(name = "Personal Journal", colorHex = "#43A047", iconName = "book")
            )

            noteDao.insertNote(
                Note(
                    notebookId = personalId,
                    title = "Welcome to Notebook 📓",
                    content = "Here is your quiet corner to capture thoughts, build checklists, and organize ideas.\n\n• Organize notes into custom notebooks with distinct colors and icons.\n• Pin essential notes to the top.\n• Switch to checklist mode for quick to-dos.\n• Search effortlessly by title or keywords.\n\nHappy writing!",
                    isPinned = true,
                    isFavorite = true,
                    colorHex = "#FFFDF8"
                )
            )

            noteDao.insertNote(
                Note(
                    notebookId = workId,
                    title = "Weekly Focus & Goals",
                    content = "- [x] Set up new workspace\n- [ ] Draft design overview\n- [ ] Review quarterly milestones\n- [ ] Schedule team coffee break",
                    isPinned = false,
                    isFavorite = false,
                    colorHex = "#FEF9E7",
                    isChecklist = true
                )
            )
        }
    }
}
