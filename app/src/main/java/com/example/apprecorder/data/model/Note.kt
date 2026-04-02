package com.example.apprecorder.data.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single voice note.
 *
 * [id] and [createdAt] use @EncodeDefault(NEVER) so they are omitted from INSERT payloads
 * when they hold their default empty-string values — Supabase generates both automatically.
 * On SELECT they are populated by Supabase and decoded normally.
 */
@Serializable
data class Note(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val id: String = "",

    val text: String,

    @SerialName("created_at")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val createdAt: String = ""
)
