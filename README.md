# Final Project — AchieveIt: Cross-Platform Achievement Tracker

Course: Desenvolvimento de Aplicações Móveis (DAM)
Student: Lucas Filipe — A51793
Date: June 2026
Repository URL: https://github.com/NalienDev/DAM-A51793-FINAL-PROJECT

---

## 1. Introduction

AchieveIt is a native Android application I developed as the final project for the Desenvolvimento de Aplicações Móveis (DAM) course. It serves as a unified cross-platform achievement and trophy tracker, aggregating gaming data from three distinct external platforms — **RetroAchievements**, **Steam**, and **PlayStation Network** — into a single, cohesive mobile experience.

This project represents a significant step beyond the course's earlier assignments, moving from isolated exercises and individual apps into a full-scale, production-quality Android application with a robust backend, cloud persistence, and an integrated AI assistant. My goal was to explore the practical integration of real-world APIs, secure cloud storage, and modern Android development paradigms under the **MVVM** architectural pattern using **Jetpack Compose** as the UI toolkit.

The app targets gamers who play across multiple platforms and want a single dashboard to track their progress, trophies, and achievements, without switching between separate apps or websites.

---

## 2. System Overview

AchieveIt is a single Android application module built entirely in **Kotlin**, structured around several clearly separated concerns:

- **Authentication Layer**: Firebase Authentication handles user identity via email and password.
- **Remote Data Sources**: Three independent API integrations — RetroAchievements, Steam Web API, and PlayStation Network — each wrapped in their own Retrofit service interface or HTTP client.
- **Offline Cache Layer**: A Room database (`achieveit_cache.db`) stores fetched game data locally for all three platforms, enabling full offline access when network connectivity is unavailable.
- **Cloud Persistence Layer**: Firebase Realtime Database stores user profile data and platform credentials (API keys, usernames, tokens) securely per authenticated user.
- **AI Assistant**: An integrated AI chat interface ("Trophie") backed by an LLM API call, offering gaming-focused conversational assistance.
- **UI Layer**: Fully built with Jetpack Compose, organized into distinct composable screens connected via a shared bottom navigation bar.

---

## 3. Architecture and Design

### 3.1 MVVM Pattern

AchieveIt strictly follows the **Model-View-ViewModel (MVVM)** architectural pattern, which is the recommended approach for modern Android development. This separation of concerns is applied consistently across every feature of the app:

- **Model**: Composed of the data layer — Room entities, Retrofit interfaces, Firebase repository classes, and data classes representing API responses. The model layer is entirely unaware of the UI.
- **ViewModel**: Acts as the bridge between the model and the UI. Each major screen has a dedicated ViewModel that holds and exposes UI state via `StateFlow` and `MutableStateFlow`. ViewModels survive configuration changes (screen rotations) and manage Coroutine scopes through `viewModelScope`.
- **View (Composables)**: Jetpack Compose composables observe ViewModel state using `collectAsState()`. They are purely reactive — they render whatever the ViewModel tells them to, and invoke ViewModel functions in response to user interaction. No business logic lives inside composables.

### 3.2 Jetpack Compose UI

The entire UI is built with **Jetpack Compose**, Android's modern declarative UI framework. This eliminates XML layout files entirely in favor of composable functions written in Kotlin. The design language is a dark-mode first aesthetic with a deep purple accent palette, leveraging `RoundedCornerShape`, `LinearProgressIndicator`, `LazyVerticalGrid`, `LazyRow`, and `LazyColumn` throughout to build fluid, responsive layouts.

### 3.3 Navigation

Navigation between screens is managed by the **Jetpack Navigation Compose** library. A bottom navigation bar provides access to the four main destination screens (Home, Library, Trophie AI, Profile), while a `NavHost` with a `NavController` handles screen transitions and back-stack management, including routes to Login, Sign-Up, Game Detail, Edit Profile, and Settings screens.

### 3.4 Coroutines and Flows

All asynchronous operations — network calls, database queries, Firebase reads and writes — are handled using **Kotlin Coroutines** and **Kotlin Flows**. ViewModels launch coroutines in `viewModelScope`, ensuring they are automatically cancelled when the ViewModel is cleared. `callbackFlow` is used to bridge Firebase's callback-based Realtime Database listeners into idiomatic Kotlin Flows, enabling reactive UI updates whenever cloud data changes.

---

## 4. Components

### 4.1 Firebase — Authentication

User identity is managed through **Firebase Authentication** using the **Email/Password** provider. On launch, the app checks whether a user is currently signed in via `FirebaseAuth.getInstance().currentUser`. If no authenticated session exists, the user is redirected to the Login screen. The Login and Sign-Up screens call `signInWithEmailAndPassword()` and `createUserWithEmailAndPassword()` respectively, with results handled via task listeners. Logout calls `auth.signOut()` and navigates back to the Login route.

### 4.2 Firebase — Realtime Database

The **Firebase Realtime Database** is used as the cloud storage layer for all user-specific configuration data. The database tree is structured per authenticated user under `/users/{uid}/`:

- `/users/{uid}/profile/` — stores `displayName`, `bio`, and `avatarUrl` for the in-app profile.
- `/users/{uid}/integrations/retroachievements/` — stores the RetroAchievements `username` and `apiKey`.
- `/users/{uid}/integrations/steam/` — stores the Steam `steamId` and `apiKey`.
- `/users/{uid}/integrations/playstation/` — stores the PlayStation `npsso` token and the derived OAuth `accessToken` and `refreshToken`.

All reads are implemented as real-time reactive Flows using `addValueEventListener` wrapped in `callbackFlow`, meaning any change made in the database (e.g., adding credentials from another device) is immediately reflected in the app's UI without requiring a manual refresh.

### 4.3 Offline Database — Room

The local offline cache is powered by **Room**, Android's SQLite abstraction library. The database class `AchieveItDatabase` declares three entities and their corresponding DAOs:

- `RaGameEntity` / `RaGameDao` — caches RetroAchievements game data.
- `PsnGameEntity` / `PsnGameDao` — caches PlayStation Network trophy data.
- `SteamGameEntity` / `SteamGameDao` — caches Steam achievement data.

Each repository follows a **cache-first** strategy: on app launch, the UI is populated immediately from the Room database (zero perceived load time), while a network refresh runs in the background and writes updated data back into Room. The `LibraryViewModel` exposes an `isOffline` flag, and the Library screen shows a visible "Offline — showing cached data" indicator if the network refresh fails, ensuring the user always has access to their last-known data.

### 4.4 RetroAchievements API

**RetroAchievements** is a community platform for tracking achievements on classic and retro consoles. The app integrates with the official RetroAchievements Web API via **Retrofit2** and the **Gson** converter.

The `RetroAchievementsApi` Retrofit interface exposes two endpoints:

- `API_GetUserCompletionProgress.php` — fetches a paginated list of all games ever played, with achievement counts and completion percentages. Supports up to 500 results per request.
- `API_GetGameInfoAndUserProgress.php` — fetches extended game metadata plus per-achievement unlock status for a specific game, used when navigating to the Game Detail screen.

Authentication is handled via query parameters `z` (caller username) and `y` (API key), both retrieved from the Firebase-stored credentials at runtime.

I personally researched and read through the RetroAchievements API documentation to discover the correct endpoint structure, the unconventional parameter naming conventions (`z`, `y`, `u`, `g`, `c`, `o`), and how the API handles pagination.

### 4.5 Steam Web API

**Steam** integration allows users to view their Steam game library alongside their achievement completion rates. The Steam Web API is accessed via **Retrofit2** using the Steam ID and personal Web API key.

The integration fetches owned games using the `GetOwnedGames` endpoint and then individually enriches each game with achievement statistics from the `GetPlayerAchievements` endpoint. Games without any achievements are excluded from the library view to keep the experience focused on achievement tracking.

Steam game data is normalized into the shared `LibraryGame` model, with achievement count used to calculate a completion fraction displayed as a progress bar throughout the app.

I researched the Steam Web API developer portal to understand the correct endpoint URLs, the required parameters (`steamid`, `key`, `include_appinfo`, `include_played_free_games`), and the behavior of the API around games with no achievements defined.

### 4.6 PlayStation Network API

**PlayStation Network** integration is the most complex of the three, as Sony does not provide an official public API. The app uses an unofficial PSN API approach based on the **NPSSO token** — a session token obtained from the PlayStation website — which is then exchanged for an OAuth `accessToken` and `refreshToken`.

The `npsso` token is entered in the Settings screen and persisted to Firebase. The app exchanges it for a short-lived access token on demand, using it to query the trophy list. The PSN integration retrieves a list of trophy titles (games), each with earned and total trophy counts and a progress percentage.

The PSN entity includes fields for `npCommunicationId`, `title`, `imageIcon`, `totalEarned`, `totalTrophies`, `progress`, and `lastUpdated`, all of which are persisted to Room for offline access.

I researched the unofficial PSN authentication flow — specifically how to obtain the NPSSO cookie from the PlayStation website and how it maps to OAuth tokens — which was essential to making this integration work.

### 4.7 AI Assistant — Trophie

**Trophie** is the app's integrated AI gaming assistant, accessible via the dedicated Trophie screen. It provides a conversational chat interface where users can ask gaming-related questions, seek advice on trophy hunting strategies, or inquire about the AchieveIt app itself.

The AI layer is built around the `AIAssistant` interface, which defines a contract implemented by concrete classes for different LLM providers. The interface provides:

- `buildPrompt(input)` — constructs a structured system prompt that defines Trophie's personality: a concise, friendly gaming assistant that stays on topic, doesn't over-explain, and skips greetings in favor of direct answers.
- `processInput(input)` — the public entry point that formats the prompt and delegates to `apiCallWithBackoff`.
- `apiCallWithBackoff(input)` — implements an exponential backoff retry mechanism, automatically retrying on HTTP 429 (Too Many Requests) errors up to 5 times with increasing delays (`baseDelay * 2^attempt`).
- `makeApiCall(prompt)` — executes the HTTP request via OkHttp and parses the JSON response, handling both OpenAI-compatible (`choices[].message.content`) and Gemini-compatible (`candidates[].content.parts[].text`) response schemas.
- `processSentiment(input)` — a secondary capability that performs sentiment analysis on a given text, returning a JSON object with a rating (1–7) and justification.

AI credentials (API key and model identifier) are loaded at runtime from the app's properties configuration, keeping secrets out of the compiled binary and source code. The `AIAssistantFactory` selects the appropriate implementation at startup.

I was responsible for researching which AI model to use, obtaining the API key, studying the required API call structure and parameters, and defining the assistant's personality and behavioral constraints through the system prompt.

---

## 5. Implementation

### 5.1 Data Layer

The data layer is organized into five subdirectories:

- `data/ai/` — `AIAssistant` interface, `AIAssistantOpenAI` implementation, `AIAssistantFactory`, and `Utils`.
- `data/local/` — `AchieveItDatabase`, and per-platform subdirectories each containing an Entity and a DAO.
- `data/model/` — data classes representing raw API response shapes (deserialized by Gson/Retrofit).
- `data/remote/` — Retrofit interfaces and client builders for RetroAchievements, Steam, and PlayStation.
- `data/repository/` — `RaRepository`, `SteamRepository`, `PsnRepository`, and `UserPrefsRepository`, each responsible for coordinating between the network and Room cache.

### 5.2 UI Layer

The UI layer is organized into five subdirectories:

- `ui/screens/` — all composable screen functions.
- `ui/viewmodel/` — `LibraryViewModel`, `GameDetailViewModel`, `ProfileViewModel`, and `AIChatViewModel`.
- `ui/models/` — shared UI-layer data models such as `LibraryGame`, `Platform`, and `ChatMessage`.
- `ui/navigation/` — navigation graph and route definitions.
- `ui/theme/` — color palette, typography, and Material3 theme configuration.

### 5.3 ViewModels in Detail

**`LibraryViewModel`**: The most complex ViewModel, responsible for aggregating data from all three platforms into a unified `List<LibraryGame>`. It combines five reactive flows (`_allGames`, `_selectedTabIndex`, `_searchQuery`, `_isOffline`, `_hasCredentials`) using `combine()` to derive the `uiState` flow. On init it launches parallel coroutine jobs using `supervisorScope` and `async`/`await`, so that a failure from one platform (e.g., PSN) does not block data from the other two.

**`GameDetailViewModel`**: Fetches detailed achievement data for a specific game from the appropriate API (based on platform) and exposes it as a UI state. For RetroAchievements, it calls `getGameInfoAndUserProgress` to retrieve per-achievement unlock details.

**`ProfileViewModel`**: Observes the user's profile data from `UserPrefsRepository`, computes statistics (completion rate, perfect games, platinum trophies, total playtime) derived from the library data, and exposes them as a combined UI state.

**`AIChatViewModel`**: Maintains a mutable list of `ChatMessage` objects representing the conversation history. On `sendMessage()`, it appends the user's message to the list, sets `isLoading = true`, launches a coroutine to call `AIAssistant.processInput()`, then appends the AI's response and clears the loading state.

---

## 6. Main Views

### 6.1 Login / Sign-Up Screen

The Login and Sign-Up screens are the app's entry point for unauthenticated users. The Login screen presents a styled hero image at the top, followed by labeled email and password text fields with leading icons. The "Login to Your Account" button calls `FirebaseAuth.signInWithEmailAndPassword()` and shows an inline loading state while the request is in flight. Errors are surfaced as Android `Toast` messages. A "Sign up for free" link navigates to the Sign-Up screen, which mirrors the layout and calls `createUserWithEmailAndPassword()` to register a new account.

### 6.2 Home Screen

The Home screen is the dashboard landed on after successful authentication. It greets the user by their display name (fetched from `ProfileViewModel`), and presents three summary stat cards:

- **Total Games** — the total number of games across all connected platforms.
- **Achievements** — the cumulative count of earned achievements/trophies.
- **Completion Rate** — the overall average completion percentage.

Below the stats, a **"Continue Playing"** horizontal scrollable row displays games currently in progress (between 0% and 100% completion), each shown as a card with the game's cover image, a completion progress bar, and the platform name. A logout button in the top bar calls `FirebaseAuth.signOut()` and routes back to Login.

### 6.3 Library Screen

The Library screen is the heart of the application — a scrollable, filterable grid of all games across connected platforms. Key features:

- **Platform Tabs**: A scrollable tab row allows filtering by "All", "RetroAchievements", "Steam", and "PlayStation".
- **Search**: An expandable search bar filters the displayed games by title in real time using `_searchQuery` StateFlow.
- **Game Cards**: Each game is displayed in a 2-column grid with cover art, a platform badge (color-coded by platform), a "Mastered" gold trophy badge for 100% completed games, and a progress bar showing earned/total achievements.
- **Offline Mode**: If the network refresh fails, a `WifiOff` icon banner is shown at the top of the list, indicating that cached data is being displayed.
- **No Credentials State**: If no platforms are connected, a prompt screen guides the user to open Settings and add their credentials.
- **Manual Refresh**: A "Refresh" button in the header triggers `viewModel.refresh()` which re-runs all parallel network calls.

### 6.4 Trophie Screen (AI Assistant)

The Trophie screen hosts the AI chat interface. It features:

- A `CenterAlignedTopAppBar` with the assistant's name.
- A `LazyColumn` that renders the conversation history as styled message bubbles, auto-scrolling to the latest message using `LaunchedEffect` and `animateScrollToItem`.
- User messages appear right-aligned with a purple background. AI responses appear left-aligned with a dark surface background.
- A bottom input area with a multi-line `TextField` and a send `IconButton` that is disabled while a response is loading.
- A `CircularProgressIndicator` that appears in the message list while the AI is processing a response.

The assistant's name is "Trophie", and its behavior is defined by the system prompt in `buildPrompt()`: it focuses exclusively on gaming and AchieveIt-related topics, keeps answers concise, and skips greetings.

### 6.5 Profile Screen

The Profile screen displays the authenticated user's gaming identity and statistics. It includes:

- A circular avatar with a neon gradient border (purple to teal to light purple), loading the avatar URL via Coil's `AsyncImage`. Defaults to a generic person icon if no avatar is set.
- The display name and a "Member since" badge.
- A bio text field.
- A 2×2 grid of stat cards showing: **Total Playtime**, **Completion Rate**, **Perfect Games** (100% completion), and **Platinum Trophies**.
- Navigation rows for **Edit Profile** (navigates to `EditProfileScreen`) and **Logout**.

All profile data is sourced from Firebase Realtime Database via `ProfileViewModel` and `UserPrefsRepository`, ensuring it reflects the actual stored profile across sessions and devices.

---

## 7. Prompting Strategy

My AI-assisted development of AchieveIt relied on a structured, goal-driven prompting approach throughout the project. Rather than issuing single large prompts, I followed an iterative loop of scoped feature prompts:

- **Architecture prompts** defined the overall MVVM structure, the package layout, and the data flow between layers early on — establishing a scaffold before any individual feature was built.
- **Component prompts** targeted individual files or features: "Implement the Room database with three entities for RetroAchievements, Steam, and PlayStation", "Build the LibraryViewModel combining three platform repositories into a unified game list using Kotlin Flows", etc.
- **Refinement prompts** addressed specific behavior: "Add an offline badge to the Library screen when the network is unavailable", "Implement exponential backoff retry in the AI assistant for HTTP 429 errors".
- **Integration prompts** connected disparate components: "Wire the ProfileViewModel to read from both UserPrefsRepository and LibraryViewModel to compute completion rate and perfect game stats".

The AI generated the vast majority of implementation code, including all Compose UI, ViewModel logic, Room schema, Retrofit interfaces, Firebase flows, and the AI assistant's backoff mechanism. My primary contributions were in research and decision-making for the three API integrations and the AI assistant configuration.

---

## 8. Autonomous Agent Workflow

The AI agent (Antigravity) managed the scaffolding and implementation of AchieveIt's core systems autonomously:

- **Project Initialization**: Bootstrapped the Jetpack Compose project structure, established the MVVM package layout, and configured the base Material3 theme and color palette.
- **Data Layer Generation**: Produced all Room entities, DAOs, Retrofit interface definitions, and repository classes for all three platforms from high-level descriptions of the desired data shapes and API endpoints.
- **ViewModel Logic**: Implemented complex reactive logic in `LibraryViewModel` — combining five `StateFlow` streams with `combine()`, running parallel network refreshes with `supervisorScope`, and mapping heterogeneous API response models into the unified `LibraryGame` model.
- **UI Composition**: Built all composable screens, including the game card grid, chat bubble layout, profile stat cards, and the multi-step settings flow for managing platform credentials.
- **Firebase Integration**: Implemented real-time Kotlin Flow wrappers around Firebase Realtime Database listeners using `callbackFlow`, handling subscription and unsubscription lifecycle correctly.
- **AI Assistant Framework**: Designed the `AIAssistant` interface with the backoff mechanism, response parsing for multiple API schemas, and the sentiment analysis capability.

I reviewed each stage to verify correctness, catch structural issues (e.g., credential flow edge cases, offline state handling), and provide direction for the next feature iteration.

---

## 9. Human vs AI Contribution

| Component | Primary Author |
|---|---|
| RetroAchievements API — endpoint research & parameter discovery | Human |
| Steam Web API — endpoint research, parameter structure | Human |
| PSN NPSSO token flow — research & authentication approach | Human |
| AI Assistant — API key sourcing, model selection | Human |
| AI Assistant — personality definition & system prompt | Human |
| Firebase Authentication setup | AI (Antigravity) |
| Firebase Realtime Database schema & Flows | AI (Antigravity) |
| Room database — entities, DAOs, migrations | AI (Antigravity) |
| Retrofit interfaces (all 3 platforms) | AI (Antigravity) |
| Repository classes (RA, Steam, PSN, UserPrefs) | AI (Antigravity) |
| MVVM ViewModels (Library, Detail, Profile, AI Chat) | AI (Antigravity) |
| Jetpack Compose UI — all screens | AI (Antigravity) |
| Navigation graph | AI (Antigravity) |
| AI Assistant backoff mechanism & response parser | AI (Antigravity) |
| Debugging & integration refinement | Human + AI |
| Documentation | Human (+ AI review context) |

---

## 10. Ethical and Responsible Use

- **Understanding First**: Although the AI generated the majority of the application code, I ensured a working understanding of each system's logic before incorporating it. All Firebase flows, Room schema decisions, and ViewModel state management patterns were reviewed and understood before being considered complete.
- **Research Responsibility**: The three external API integrations required substantial research on my part — reading the official RetroAchievements API documentation, the Steam Web API developer portal, and community documentation for the unofficial PSN flow. The AI cannot reliably discover or validate undocumented API behaviors, making my research an essential and non-delegatable contribution.
- **AI Configuration**: The choice of LLM model, the API key management strategy, and the assistant's behavioral constraints (personality, topic scope, response style) were all my decisions. I researched the available models, evaluated their suitability for a gaming assistant use case, and defined the system prompt accordingly.
- **Data Security**: Platform credentials (API keys, NPSSO tokens) are never hardcoded in the application. They are stored per-user in Firebase Realtime Database, secured by Firebase Authentication rules, ensuring no user can access another user's credentials.

---

# Development Process

## 11. Version Control and Commit History

I managed the project using Git, with the repository hosted on GitHub. The repository was initialized and connected via Android Studio's built-in Source Control panel. Commits were made incrementally at meaningful milestones — initial project scaffold, database schema finalization, each API integration, Firebase integration, AI assistant implementation, and UI polish passes.

The `config.properties` file containing API keys was tracked initially but was subsequently removed from the repository via `git rm --cached` to prevent credential exposure, and added to `.gitignore` to prevent future accidental commits.

## 12. Difficulties and Lessons Learned

**Offline Cache Not Loading on Startup**
One of the more subtle issues encountered was that the Room cache was being populated correctly, but the app would not load that data on startup: it would simply show an empty library until a network response arrived. The root cause was that Firebase Realtime Database, by default, requires an active network connection before serving any data, bypassing the local Room cache entirely on the first read. The fix was to explicitly enable Firebase's own disk persistence via `FirebaseDatabase.getInstance().setPersistenceEnabled(true)`, which allows Firebase to serve locally cached data immediately on launch while the network refresh runs in the background. This reinforced the importance of understanding the initialization order between Firebase and the local cache layer.

**PlayStation Network Integration**
The PSN integration was by far the most challenging of the three platforms. Unlike RetroAchievements and Steam, Sony does not provide an official public API, which meant relying on community-documented authentication flows and unofficial endpoints. The most time-consuming part was identifying a working OAuth client ID and secret, as the available documentation is fragmented and frequently outdated. This integration required significant trial and error and taught me to approach undocumented APIs with patience and a systematic testing mindset.

**AI Assistant — API Connectivity**
The initial AI assistant implementation targeted the NVIDIA NIM API, but the app was unable to reach it from the Android HTTP client. After investigation, the solution was to switch to **Groq** as the inference provider, which exposes an OpenAI-compatible API surface — meaning the existing `AIAssistantOpenAI` implementation required no structural changes beyond updating the base URL and model identifier. This highlighted the practical value of designing the `AIAssistant` abstraction layer around a provider-agnostic interface from the start.

## 13. Future Improvements

- **Playtime Tracking**: Surface individual game playtime on the Game Detail screen for Steam games, where the API already exposes this data, giving users a more complete picture of each game's history alongside its achievement progress.
- **RetroAchievements Points**: Display the point value of each individual achievement on the Game Detail screen, and show total points earned alongside the existing completion percentage.
- **Social Features**: Allow users to follow other AchieveIt accounts, view their public library, and compare completion rates on shared games, adding a competitive and community dimension to the tracker.
- **Multi-Account Support**: Enable the addition of multiple accounts per platform (e.g., two Steam libraries or two PSN profiles) to support users who maintain separate gaming accounts.
- **Additional Platform Integrations**: Extend the tracker to cover further platforms such as Xbox (via the OpenXBL API), GOG Galaxy, and Epic Games Store, consolidating an even broader range of gaming activity into a single dashboard.
- **Achievement Sorting and Filtering**: Add sorting options to the Game Detail screen (by unlock date, rarity, point value) so users can plan their next achievements more strategically.

## 14. AI Usage Disclosure

| Tool | How it was used |
|---|---|
| **Antigravity** | Primary development tool. Generated the full application architecture including MVVM scaffolding, Room database, Retrofit integrations, Firebase flows, Jetpack Compose UI, ViewModels, and the AI assistant framework. Iterated on features through refinement prompts throughout the project. |
| **Claude / ChatGPT** | Consulted for targeted questions regarding specific Kotlin syntax nuances, Compose API details, and debugging assistance when resolving integration edge cases. |

I confirm responsibility for the artifact contents produced utilizing generative tools where listed, fully understanding all codebase logics utilized structurally, and having performed the required research for the API integrations and AI assistant configuration that the tools could not autonomously provide.
