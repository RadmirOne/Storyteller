# Project Context: Storyteller Engine

This document serves as the central context for the Storyteller project, outlining the architecture and logic implemented for the story script parsing system.

## Project Overview
- **Technology Stack**: Kotlin Multiplatform (KMP), Compose Multiplatform.
- **Core Goal**: Build a robust, data-driven story engine capable of parsing complex scripts with branching paths, conditions, and state persistence.
- **Serialization**: Powered by `kotlinx-serialization` for JSON-based script loading.

## Data Models (`shared/src/commonMain/kotlin/com/radmir/storyteller/models/`)

### 1. `StoryScript`
The root object representing a full story.
- `title`: Name of the story.
- `startNodeId`: The ID of the node where the story begins.
- `nodes`: A map of IDs to `DialogueNode` objects.

### 2. `DialogueNode`
A single point in the story.
- `id`: Unique identifier.
- `speaker`: The entity speaking.
- `text`: The content of the dialogue.
- `choices`: A list of possible player actions (branching).
- `nextNodeId`: For linear progression (used when no choices are present).

### 3. `Choice`
A branching option for the player.
- `id`: Unique identifier for selection.
- `text`: The text displayed to the player.
- `targetNodeId`: The ID of the node this choice leads to.
- `conditions`: Optional map of requirements (e.g., `{"affinity": 10}`) that must be met to see/select this choice.

### 4. `GameState`
Tracks the current progress and variables.
- `currentNodeId`: The current active node.
- `variables`: A map of integers (e.g., `affinity`, `health`) that can be modified by the story.
- `visitedNodes`: A set of IDs for nodes already visited.

## Core Components

### `StoryRepository`
- Responsible for loading and holding the `StoryScript`.
- Provides `loadScript(json: String)` to initialize the data.

### `StoryEngine`
The brain of the application. Manages the lifecycle of the story:
- **Initialization**: Takes a JSON string, loads it via the repository, and sets the initial `GameState`.
- **Navigation**:
    - `getCurrentNode()`: Returns the `DialogueNode` based on the current `GameState`.
    - `selectChoice(choiceId)`: Validates conditions, updates the `visitedNodes` set, and moves the `currentNodeId` to the target.
    - `advance()`: Moves to the next linear node if `nextNodeId` is present.

### `StoryViewModel`
The bridge between the engine and the UI.
- Exposes `gameState` and `currentNode` as `StateFlow` for reactive UI updates.
- Handles user actions (`selectChoice`, `advance`) and triggers state updates in the `StoryEngine`.

## UI Layer

### `StoryScreen`
- A Compose Multiplatform component that displays the current dialogue.
- Automatically updates UI elements (speaker, text, buttons) based on the `StoryViewModel` state.
- Supports linear progression buttons and choice lists.

## Logic Flow
1. **Load**: `Res.readBytes("story.json")` $\rightarrow$ `StoryRepository.loadScript()` $\rightarrow$ `StoryScript`
2. **Init**: `StoryEngine.initialize()` $\rightarrow$ `GameState(startNodeId)`
3. **Update**: Player selects `Choice` $\rightarrow$ `StoryViewModel.selectChoice()` $\rightarrow$ `StoryEngine.selectChoice()` $\rightarrow$ `GameState` updated.
4. **Render**: UI observes `StoryViewModel` states and displays the data.

## Current Status
- [x] Project configuration (Dependencies & Plugins).
- [x] Core Data Models.
- [x] Repository and Engine logic.
- [x] Sample JSON Story Script.
- [x] ViewModel and Compose UI.
- [ ] Advanced logic (variables, persistence).
- [ ] Polished UI/Animations.
