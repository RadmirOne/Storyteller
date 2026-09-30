# Story audio

Scenes optionally declare looping packaged audio:

```json
"audio": {
  "music": "files/audio/theme.mp3",
  "ambience": "files/audio/sea.wav"
}
```

Omitted/null tracks mean silence. The same path continues across dialogue and scene changes; a different path replaces that channel. Every channel loops. WAV, MP3 and M4A are supported; put assets under `shared/src/commonMain/composeResources/files/audio`. The decoder must support the actual encoding. Validation rejects URLs, traversal and unsupported extensions; missing/corrupt resources invoke `onError` without blocking reading.

Mount `StoryAudio(state, AudioSettings(...))` once in the reader/app. Pass null outside reading. `AudioSettings` controls enabled, musicVolume and ambienceVolume (0–1). The reader settings can supply the same persisted volume for both channels. Android uses MediaPlayer; iOS uses AVAudioPlayer. No network or background-audio permission is required. iOS retains the default ambient session behavior, respecting the silent switch.

Playback pauses below lifecycle RESUMED and resumes from the same position when active. Muting likewise pauses instead of rebuilding a player. Disposal releases both channels. Playback position is session-only: reopening the app starts the current scene tracks from the beginning. There are no one-shot sound effects or crossfades yet. Decode/preparation occurs synchronously after resource loading, so keep initial assets small; larger music assets should use asynchronous preparation before release.

`sea.wav` is an original 12-second synthetic filtered-noise loop, generated with fixed seed by `tools/generate-ambience.ps1`, with no recordings or third-party samples. It is an understated placeholder atmosphere for the demo, not a realistic field recording.

Common tests exercise backgrounding during load, mute/resume without reload, release before late load and resource validation. Android host tests/build verify Kotlin compilation. Real-device listening, interruptions (calls/headphones), and the iOS build/runtime require device/macOS QA. The app lifecycle handles backgrounding; platform audio-focus/interruption callbacks are not implemented in this first version.

Before opening a story, call suspend validateStoryAudioResources(script, loadBytes). It reads each unique audio resource once, rejects empty or unreadable files, and checks WAV container/chunk boundaries plus format and data presence. This is lightweight preflight, not codec decoding. A platform playback failure releases that channel and reports one error; recomposition cannot repeatedly retry the failed player.

The demo now includes `lighthouse-theme.wav`: an original 32-second, mono 16 kHz PCM ambient melody over four chords. Reproduce it with `python tools/generate-demo-music.py` (Python standard library only). Oscillators, envelopes and wrapped release tails are deterministic; no recordings or third-party samples are used. The same music path is assigned to every lighthouse scene so it does not restart at scene boundaries. Outdoor scenes retain sea ambience. Device listening and subjective mix/loop approval remain pending.
