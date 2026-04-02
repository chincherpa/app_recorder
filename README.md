# AppRecorder

An Android app that turns spoken words into notes. Tap the microphone, speak, and the transcribed text is saved to a Supabase backend — no typing required.

## Features

- **Live transcription** — partial results appear on screen as you speak
- **One-tap recording** — mic button toggles recording on/off; requests `RECORD_AUDIO` permission automatically on first use
- **Cloud storage** — notes are persisted in Supabase (PostgREST) and loaded on startup, newest first
- **Material 3 UI** — built entirely with Jetpack Compose

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Speech | Android `SpeechRecognizer` API |
| Backend | Supabase (PostgREST) |
| HTTP | Ktor (OkHttp engine) |
| Serialization | kotlinx.serialization |

## Requirements

- Android 8.0 (API 26) or higher
- A [Supabase](https://supabase.com) project with a `notes` table

## Setup

### 1. Create the Supabase table

Run this SQL in your Supabase project's SQL editor:

```sql
create table notes (
  id         uuid primary key default gen_random_uuid(),
  text       text not null,
  created_at timestamptz not null default now()
);
```

### 2. Add your Supabase credentials

Open [app/src/main/java/com/example/apprecorder/data/remote/SupabaseClient.kt](app/src/main/java/com/example/apprecorder/data/remote/SupabaseClient.kt) and replace the placeholder values:

```kotlin
private const val SUPABASE_URL = "https://<your-project-ref>.supabase.co"
private const val SUPABASE_ANON_KEY = "<your-anon-key>"
```

Both values can be found in your Supabase dashboard under **Project Settings → API**.

### 3. Build and run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and run it on a device or emulator.

## Project Structure

```
app/src/main/java/com/example/apprecorder/
├── data/
│   ├── model/          Note.kt          — data class mapped to Supabase row
│   └── remote/         NoteRepository.kt, SupabaseClient.kt
├── speech/             SpeechRecognizerManager.kt
├── ui/
│   ├── screens/        HomeScreen.kt, components/NoteCard.kt
│   ├── theme/          Color.kt, Theme.kt, Type.kt
│   ├── NoteUiState.kt
│   └── NoteViewModel.kt
└── MainActivity.kt
```
