package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notebookId: Long? = null,
    val title: String = "",
    val content: String = "",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val colorHex: String = "#FFFDF8",
    val tags: String = "",
    val isChecklist: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
