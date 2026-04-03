# Notes Web Admin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Next.js web app on Vercel for reading, creating, editing, and deleting notes from the existing Supabase `notes` table.

**Architecture:** Single-page Next.js (App Router) app using the Supabase JS client directly from the browser. All state lives in `app/page.tsx` via `useState`/`useEffect`. Components are pure presentational with callback props.

**Tech Stack:** Next.js 14 (App Router), TypeScript, Tailwind CSS, `@supabase/supabase-js`, Jest + React Testing Library

---

## File Map

| File | Responsibility |
|------|----------------|
| `types/note.ts` | `Note` TypeScript interface |
| `lib/supabase.ts` | Supabase client singleton |
| `components/AddNoteForm.tsx` | Controlled input + submit button for new notes |
| `components/NoteRow.tsx` | Single note: display, inline edit, delete |
| `components/NoteList.tsx` | Renders list of `NoteRow`, handles empty state |
| `app/page.tsx` | Fetches data, owns all state, passes callbacks down |
| `jest.config.ts` | Jest + next/jest configuration |
| `jest.setup.ts` | Imports `@testing-library/jest-dom` matchers |

---

## Task 1: Scaffold the project

**Files:**
- Create: `next-notes-admin/` (new directory, sibling to `app_recorder/`)

- [ ] **Step 1: Run create-next-app**

Run from the parent directory of `app_recorder` (e.g. `d:/Projects/`):

```bash
npx create-next-app@latest next-notes-admin \
  --typescript \
  --tailwind \
  --eslint \
  --app \
  --no-src-dir \
  --import-alias "@/*" \
  --no-turbopack
cd next-notes-admin
```

When prompted interactively, accept all defaults.

- [ ] **Step 2: Install Supabase JS client**

```bash
npm install @supabase/supabase-js
```

- [ ] **Step 3: Install Jest and React Testing Library**

```bash
npm install --save-dev jest jest-environment-jsdom @testing-library/react @testing-library/jest-dom @testing-library/user-event
```

- [ ] **Step 4: Create `.env.local`**

Create `next-notes-admin/.env.local` with these values (from the Android app's `SupabaseClient.kt`):

```
NEXT_PUBLIC_SUPABASE_URL=https://hlluhoxrvvqmubwlorxm.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhsbHVob3hydnZxbXVid2xvcnhtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzUxMjM5NDIsImV4cCI6MjA5MDY5OTk0Mn0.3DtpOxYCbG64tGpd_LRknVJcbdUTNnI1xQPCXd3z348
```

Add `.env.local` to `.gitignore` (should already be there from scaffold).

- [ ] **Step 5: Create `jest.config.ts`**

```typescript
import type { Config } from 'jest'
import nextJest from 'next/jest.js'

const createJestConfig = nextJest({ dir: './' })

const config: Config = {
  testEnvironment: 'jest-environment-jsdom',
  setupFilesAfterEnv: ['<rootDir>/jest.setup.ts'],
}

export default createJestConfig(config)
```

- [ ] **Step 6: Create `jest.setup.ts`**

```typescript
import '@testing-library/jest-dom'
```

- [ ] **Step 7: Verify the app runs**

```bash
npm run dev
```

Expected: Next.js dev server starts at `http://localhost:3000`, default page loads.

- [ ] **Step 8: Commit**

```bash
git init
git add .
git commit -m "feat: scaffold Next.js project with Supabase and testing setup"
```

---

## Task 2: Note type

**Files:**
- Create: `types/note.ts`

- [ ] **Step 1: Create the type file**

```typescript
// types/note.ts
export interface Note {
  id: number
  content: string
  created_at: string
}
```

- [ ] **Step 2: Commit**

```bash
git add types/note.ts
git commit -m "feat: add Note type"
```

---

## Task 3: Supabase client

**Files:**
- Create: `lib/supabase.ts`

- [ ] **Step 1: Create the client**

```typescript
// lib/supabase.ts
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL!
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!

export const supabase = createClient(supabaseUrl, supabaseAnonKey)
```

- [ ] **Step 2: Commit**

```bash
git add lib/supabase.ts
git commit -m "feat: add Supabase client singleton"
```

---

## Task 4: AddNoteForm component

**Files:**
- Create: `components/AddNoteForm.tsx`
- Create: `__tests__/components/AddNoteForm.test.tsx`

- [ ] **Step 1: Write the failing test**

```typescript
// __tests__/components/AddNoteForm.test.tsx
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AddNoteForm from '@/components/AddNoteForm'

describe('AddNoteForm', () => {
  it('renders an input and a submit button', () => {
    render(<AddNoteForm onAdd={jest.fn()} loading={false} />)
    expect(screen.getByPlaceholderText('Neue Notiz...')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Hinzufügen' })).toBeInTheDocument()
  })

  it('calls onAdd with the trimmed text and clears the input on submit', async () => {
    const user = userEvent.setup()
    const onAdd = jest.fn().mockResolvedValue(undefined)
    render(<AddNoteForm onAdd={onAdd} loading={false} />)

    await user.type(screen.getByPlaceholderText('Neue Notiz...'), '  Testnotiz  ')
    await user.click(screen.getByRole('button', { name: 'Hinzufügen' }))

    expect(onAdd).toHaveBeenCalledWith('Testnotiz')
    expect(screen.getByPlaceholderText('Neue Notiz...')).toHaveValue('')
  })

  it('does not call onAdd when input is blank', async () => {
    const user = userEvent.setup()
    const onAdd = jest.fn()
    render(<AddNoteForm onAdd={onAdd} loading={false} />)

    await user.click(screen.getByRole('button', { name: 'Hinzufügen' }))

    expect(onAdd).not.toHaveBeenCalled()
  })

  it('disables input and button when loading', () => {
    render(<AddNoteForm onAdd={jest.fn()} loading={true} />)
    expect(screen.getByPlaceholderText('Neue Notiz...')).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Hinzufügen' })).toBeDisabled()
  })
})
```

- [ ] **Step 2: Run to confirm it fails**

```bash
npx jest AddNoteForm --no-coverage
```

Expected: FAIL — `Cannot find module '@/components/AddNoteForm'`

- [ ] **Step 3: Implement the component**

```typescript
// components/AddNoteForm.tsx
'use client'
import { useState } from 'react'

interface Props {
  onAdd: (text: string) => Promise<void>
  loading: boolean
}

export default function AddNoteForm({ onAdd, loading }: Props) {
  const [text, setText] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!text.trim()) return
    await onAdd(text.trim())
    setText('')
  }

  return (
    <form onSubmit={handleSubmit} className="flex gap-2 mb-6">
      <input
        type="text"
        value={text}
        onChange={e => setText(e.target.value)}
        placeholder="Neue Notiz..."
        className="flex-1 border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        disabled={loading}
      />
      <button
        type="submit"
        disabled={loading || !text.trim()}
        className="bg-blue-600 text-white px-4 py-2 rounded text-sm hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Hinzufügen
      </button>
    </form>
  )
}
```

- [ ] **Step 4: Run to confirm it passes**

```bash
npx jest AddNoteForm --no-coverage
```

Expected: PASS — 4 tests pass

- [ ] **Step 5: Commit**

```bash
git add components/AddNoteForm.tsx __tests__/components/AddNoteForm.test.tsx
git commit -m "feat: add AddNoteForm component"
```

---

## Task 5: NoteRow component

**Files:**
- Create: `components/NoteRow.tsx`
- Create: `__tests__/components/NoteRow.test.tsx`

- [ ] **Step 1: Write the failing test**

```typescript
// __tests__/components/NoteRow.test.tsx
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import NoteRow from '@/components/NoteRow'
import { Note } from '@/types/note'

const note: Note = {
  id: 1,
  content: 'Testinhalt',
  created_at: '2026-04-03T10:00:00.000Z',
}

describe('NoteRow', () => {
  it('renders note content and a formatted date', () => {
    render(<NoteRow note={note} onUpdate={jest.fn()} onDelete={jest.fn()} />)
    expect(screen.getByText('Testinhalt')).toBeInTheDocument()
    // Date is formatted — just check a substring present in de-DE locale
    expect(screen.getByText(/2026/)).toBeInTheDocument()
  })

  it('shows Bearbeiten and Löschen buttons in view mode', () => {
    render(<NoteRow note={note} onUpdate={jest.fn()} onDelete={jest.fn()} />)
    expect(screen.getByRole('button', { name: 'Bearbeiten' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Löschen' })).toBeInTheDocument()
  })

  it('switches to edit mode on Bearbeiten click', async () => {
    const user = userEvent.setup()
    render(<NoteRow note={note} onUpdate={jest.fn()} onDelete={jest.fn()} />)

    await user.click(screen.getByRole('button', { name: 'Bearbeiten' }))

    expect(screen.getByRole('textbox')).toHaveValue('Testinhalt')
    expect(screen.getByRole('button', { name: 'Speichern' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Abbrechen' })).toBeInTheDocument()
  })

  it('calls onUpdate with new text and exits edit mode on Speichern', async () => {
    const user = userEvent.setup()
    const onUpdate = jest.fn().mockResolvedValue(undefined)
    render(<NoteRow note={note} onUpdate={onUpdate} onDelete={jest.fn()} />)

    await user.click(screen.getByRole('button', { name: 'Bearbeiten' }))
    await user.clear(screen.getByRole('textbox'))
    await user.type(screen.getByRole('textbox'), 'Geänderter Text')
    await user.click(screen.getByRole('button', { name: 'Speichern' }))

    expect(onUpdate).toHaveBeenCalledWith(1, 'Geänderter Text')
  })

  it('cancels edit with Escape key and restores original text', async () => {
    const user = userEvent.setup()
    render(<NoteRow note={note} onUpdate={jest.fn()} onDelete={jest.fn()} />)

    await user.click(screen.getByRole('button', { name: 'Bearbeiten' }))
    await user.clear(screen.getByRole('textbox'))
    await user.type(screen.getByRole('textbox'), 'Neue Eingabe')
    await user.keyboard('{Escape}')

    expect(screen.getByText('Testinhalt')).toBeInTheDocument()
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
  })

  it('calls onDelete with note id on Löschen click', async () => {
    const user = userEvent.setup()
    const onDelete = jest.fn().mockResolvedValue(undefined)
    render(<NoteRow note={note} onUpdate={jest.fn()} onDelete={onDelete} />)

    await user.click(screen.getByRole('button', { name: 'Löschen' }))

    expect(onDelete).toHaveBeenCalledWith(1)
  })
})
```

- [ ] **Step 2: Run to confirm it fails**

```bash
npx jest NoteRow --no-coverage
```

Expected: FAIL — `Cannot find module '@/components/NoteRow'`

- [ ] **Step 3: Implement the component**

```typescript
// components/NoteRow.tsx
'use client'
import { useState } from 'react'
import { Note } from '@/types/note'

interface Props {
  note: Note
  onUpdate: (id: number, text: string) => Promise<void>
  onDelete: (id: number) => Promise<void>
}

export default function NoteRow({ note, onUpdate, onDelete }: Props) {
  const [editing, setEditing] = useState(false)
  const [text, setText] = useState(note.content)

  async function handleSave() {
    if (!text.trim()) return
    await onUpdate(note.id, text.trim())
    setEditing(false)
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSave()
    }
    if (e.key === 'Escape') {
      setText(note.content)
      setEditing(false)
    }
  }

  const date = new Date(note.created_at).toLocaleString('de-DE')

  return (
    <div className="flex items-start gap-3 p-4 border border-gray-200 rounded-lg mb-2 bg-white">
      <div className="flex-1 min-w-0">
        {editing ? (
          <textarea
            value={text}
            onChange={e => setText(e.target.value)}
            onKeyDown={handleKeyDown}
            className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={3}
            autoFocus
          />
        ) : (
          <p className="text-sm text-gray-800 break-words">{note.content}</p>
        )}
        <p className="text-xs text-gray-400 mt-1">{date}</p>
      </div>
      <div className="flex gap-3 shrink-0 mt-1">
        {editing ? (
          <>
            <button
              onClick={handleSave}
              className="text-sm text-green-600 hover:underline"
            >
              Speichern
            </button>
            <button
              onClick={() => { setText(note.content); setEditing(false) }}
              className="text-sm text-gray-500 hover:underline"
            >
              Abbrechen
            </button>
          </>
        ) : (
          <>
            <button
              onClick={() => setEditing(true)}
              className="text-sm text-blue-600 hover:underline"
            >
              Bearbeiten
            </button>
            <button
              onClick={() => onDelete(note.id)}
              className="text-sm text-red-600 hover:underline"
            >
              Löschen
            </button>
          </>
        )}
      </div>
    </div>
  )
}
```

- [ ] **Step 4: Run to confirm it passes**

```bash
npx jest NoteRow --no-coverage
```

Expected: PASS — 6 tests pass

- [ ] **Step 5: Commit**

```bash
git add components/NoteRow.tsx __tests__/components/NoteRow.test.tsx
git commit -m "feat: add NoteRow component with inline edit"
```

---

## Task 6: NoteList component

**Files:**
- Create: `components/NoteList.tsx`
- Create: `__tests__/components/NoteList.test.tsx`

- [ ] **Step 1: Write the failing test**

```typescript
// __tests__/components/NoteList.test.tsx
import { render, screen } from '@testing-library/react'
import NoteList from '@/components/NoteList'
import { Note } from '@/types/note'

const notes: Note[] = [
  { id: 1, content: 'Erste Notiz', created_at: '2026-04-03T10:00:00.000Z' },
  { id: 2, content: 'Zweite Notiz', created_at: '2026-04-03T09:00:00.000Z' },
]

describe('NoteList', () => {
  it('renders all notes', () => {
    render(<NoteList notes={notes} onUpdate={jest.fn()} onDelete={jest.fn()} />)
    expect(screen.getByText('Erste Notiz')).toBeInTheDocument()
    expect(screen.getByText('Zweite Notiz')).toBeInTheDocument()
  })

  it('shows an empty state message when there are no notes', () => {
    render(<NoteList notes={[]} onUpdate={jest.fn()} onDelete={jest.fn()} />)
    expect(screen.getByText('Noch keine Notizen.')).toBeInTheDocument()
  })
})
```

- [ ] **Step 2: Run to confirm it fails**

```bash
npx jest NoteList --no-coverage
```

Expected: FAIL — `Cannot find module '@/components/NoteList'`

- [ ] **Step 3: Implement the component**

```typescript
// components/NoteList.tsx
import { Note } from '@/types/note'
import NoteRow from '@/components/NoteRow'

interface Props {
  notes: Note[]
  onUpdate: (id: number, text: string) => Promise<void>
  onDelete: (id: number) => Promise<void>
}

export default function NoteList({ notes, onUpdate, onDelete }: Props) {
  if (notes.length === 0) {
    return <p className="text-gray-400 text-sm">Noch keine Notizen.</p>
  }

  return (
    <div>
      {notes.map(note => (
        <NoteRow key={note.id} note={note} onUpdate={onUpdate} onDelete={onDelete} />
      ))}
    </div>
  )
}
```

- [ ] **Step 4: Run to confirm it passes**

```bash
npx jest NoteList --no-coverage
```

Expected: PASS — 2 tests pass

- [ ] **Step 5: Commit**

```bash
git add components/NoteList.tsx __tests__/components/NoteList.test.tsx
git commit -m "feat: add NoteList component"
```

---

## Task 7: Main page

**Files:**
- Modify: `app/page.tsx` (replace scaffold content)
- Modify: `app/layout.tsx` (set page title)

- [ ] **Step 1: Update `app/layout.tsx` title**

Find the `metadata` export in `app/layout.tsx` and update it:

```typescript
export const metadata: Metadata = {
  title: 'Notizen',
  description: 'Notizen verwalten',
}
```

Leave the rest of `layout.tsx` (fonts, body) unchanged.

- [ ] **Step 2: Replace `app/page.tsx`**

```typescript
// app/page.tsx
'use client'
import { useEffect, useState } from 'react'
import { supabase } from '@/lib/supabase'
import { Note } from '@/types/note'
import AddNoteForm from '@/components/AddNoteForm'
import NoteList from '@/components/NoteList'

export default function Home() {
  const [notes, setNotes] = useState<Note[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function fetchNotes() {
    setLoading(true)
    setError(null)
    const { data, error } = await supabase
      .from('notes')
      .select('*')
      .order('created_at', { ascending: false })
    if (error) {
      setError('Fehler beim Laden der Notizen.')
    } else {
      setNotes(data ?? [])
    }
    setLoading(false)
  }

  useEffect(() => { fetchNotes() }, [])

  async function addNote(text: string) {
    setError(null)
    const { error } = await supabase.from('notes').insert({ content: text })
    if (error) {
      setError('Fehler beim Hinzufügen der Notiz.')
    } else {
      await fetchNotes()
    }
  }

  async function updateNote(id: number, text: string) {
    setError(null)
    const { error } = await supabase
      .from('notes')
      .update({ content: text })
      .eq('id', id)
    if (error) {
      setError('Fehler beim Bearbeiten der Notiz.')
    } else {
      await fetchNotes()
    }
  }

  async function deleteNote(id: number) {
    setError(null)
    const { error } = await supabase.from('notes').delete().eq('id', id)
    if (error) {
      setError('Fehler beim Löschen der Notiz.')
    } else {
      await fetchNotes()
    }
  }

  return (
    <main className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6 text-gray-900">Notizen</h1>
      <AddNoteForm onAdd={addNote} loading={loading} />
      {error && (
        <p className="text-red-500 text-sm mb-4">{error}</p>
      )}
      {loading ? (
        <p className="text-gray-400 text-sm">Laden...</p>
      ) : (
        <NoteList notes={notes} onUpdate={updateNote} onDelete={deleteNote} />
      )}
    </main>
  )
}
```

- [ ] **Step 3: Run the full test suite**

```bash
npx jest --no-coverage
```

Expected: all tests pass (AddNoteForm: 4, NoteRow: 6, NoteList: 2)

- [ ] **Step 4: Run the dev server and manually verify**

```bash
npm run dev
```

Open `http://localhost:3000` and verify:
- Notes load from Supabase on page open
- A new note can be added and appears at the top
- A note can be edited inline and saved
- A note can be deleted

- [ ] **Step 5: Commit**

```bash
git add app/page.tsx app/layout.tsx
git commit -m "feat: wire up main page with full CRUD"
```

---

## Task 8: Push to GitHub and deploy on Vercel

**Files:** none (infrastructure only)

- [ ] **Step 1: Create GitHub repo**

Go to [github.com/new](https://github.com/new), create a **private** repo named `next-notes-admin`. Do NOT initialize with README.

- [ ] **Step 2: Push**

```bash
git remote add origin https://github.com/<your-username>/next-notes-admin.git
git branch -M main
git push -u origin main
```

- [ ] **Step 3: Import on Vercel**

1. Go to [vercel.com/new](https://vercel.com/new)
2. Click "Import Git Repository" → select `next-notes-admin`
3. Framework Preset: **Next.js** (auto-detected)
4. Before clicking Deploy, open **Environment Variables** and add:
   - `NEXT_PUBLIC_SUPABASE_URL` = `https://hlluhoxrvvqmubwlorxm.supabase.co`
   - `NEXT_PUBLIC_SUPABASE_ANON_KEY` = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhsbHVob3hydnZxbXVid2xvcnhtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzUxMjM5NDIsImV4cCI6MjA5MDY5OTk0Mn0.3DtpOxYCbG64tGpd_LRknVJcbdUTNnI1xQPCXd3z348`
5. Click **Deploy**

- [ ] **Step 4: Verify the live deployment**

Open the Vercel deployment URL and verify all CRUD operations work against the real Supabase database.

---

## Self-Review Notes

- All 4 spec features covered: Read ✓, Create ✓, Update ✓, Delete ✓
- Loading state and error messages covered in `page.tsx` ✓
- Empty state covered in `NoteList` ✓
- No auth as specified ✓
- `.env.local` in `.gitignore` ✓
- Supabase column name is `content` (matching Android model `@SerialName("content")`) ✓
