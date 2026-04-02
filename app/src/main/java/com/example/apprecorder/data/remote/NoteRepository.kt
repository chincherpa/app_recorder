package com.example.apprecorder.data.remote

import com.example.apprecorder.data.model.Note
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class NoteRepository {

    /**
     * Inserts a new note into Supabase.
     * [id] and [created_at] are omitted (Supabase generates them via column defaults).
     */
    suspend fun saveNote(text: String) {
        supabaseClient.postgrest["notes"].insert(Note(text = text))
    }

    /**
     * Fetches all notes ordered by creation time (newest first).
     */
    suspend fun fetchNotes(): List<Note> {
        return supabaseClient.postgrest["notes"]
            .select {
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<Note>()
    }
}
