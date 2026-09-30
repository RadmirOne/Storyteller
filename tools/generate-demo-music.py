"""Original 32-second ambient theme; no recordings, samples or external dependencies.
Run from any directory: python tools/generate-demo-music.py
PCM mono 16 kHz; cyclic synthesis includes note tails across the loop boundary.
"""
import math
from pathlib import Path
import struct
import wave

RATE = 16000
SECONDS = 32
samples = [0.0] * (RATE * SECONDS)

def note(midi, start, duration, gain):
    frequency = 440 * 2 ** ((midi - 69) / 12)
    for i in range(int(duration * RATE)):
        t = i / RATE
        # Zero-amplitude endpoints avoid clicks; long release gives a soft bell/pad.
        envelope = math.sin(math.pi * t / duration) ** 2 * math.exp(-t / 3)
        value = math.sin(2 * math.pi * frequency * t)
        value += .18 * math.sin(4 * math.pi * frequency * t)
        samples[(int(start * RATE) + i) % len(samples)] += gain * envelope * value

for bar, chord in enumerate(((48, 55, 60, 64), (45, 52, 57, 60), (41, 48, 53, 57), (43, 50, 55, 62))):
    for pitch in chord:
        note(pitch, bar * 8, 10, .07)
for beat, pitch in enumerate((72, 76, 79, 76, 72, 69, 67, 64, 65, 69, 72, 69, 67, 74, 71, 67)):
    note(pitch, beat * 2, 4, .1)

path = Path(__file__).resolve().parents[1] / 'shared/src/commonMain/composeResources/files/audio/lighthouse-theme.wav'
with wave.open(str(path), 'wb') as output:
    output.setparams((1, 2, RATE, 0, 'NONE', 'not compressed'))
    output.writeframes(b''.join(struct.pack('<h', round(max(-1, min(1, x)) * 32767)) for x in samples))
print(path, 'peak:', round(max(map(abs, samples)), 3))
