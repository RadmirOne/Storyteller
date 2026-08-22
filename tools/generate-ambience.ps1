# Original synthetic filtered-noise ambience. No sampled recordings or third-party assets.
$audioOutput = Join-Path $PSScriptRoot '../shared/src/commonMain/composeResources/files/audio/sea.wav'
$sampleRate = 16000
$sampleCount = $sampleRate * 12
$random = [Random]::new(4172)
$samples = [double[]]::new($sampleCount)
$filtered = 0.0
for ($i = 0; $i -lt $sampleCount; $i++) {
    $filtered = 0.94 * $filtered + 0.06 * ($random.NextDouble() * 2 - 1)
    $swell = 0.6 + 0.4 * [Math]::Pow([Math]::Sin([Math]::PI * 2 * $i / $sampleCount), 2)
    $samples[$i] = $filtered * $swell * 14000
}
# Short equal-power fades keep the loop boundary free of sample discontinuities.
$fade = 320
for ($i = 0; $i -lt $fade; $i++) {
    $factor = [Math]::Sin([Math]::PI * 0.5 * $i / $fade)
    $samples[$i] *= $factor
    $samples[$sampleCount - 1 - $i] *= $factor
}
$writer = [IO.BinaryWriter]::new([IO.File]::Create($audioOutput))
try {
    $writer.Write([Text.Encoding]::ASCII.GetBytes('RIFF'))
    $writer.Write([int](36 + $sampleCount * 2))
    $writer.Write([Text.Encoding]::ASCII.GetBytes('WAVEfmt '))
    $writer.Write([int]16)
    $writer.Write([short]1)
    $writer.Write([short]1)
    $writer.Write([int]$sampleRate)
    $writer.Write([int]($sampleRate * 2))
    $writer.Write([short]2)
    $writer.Write([short]16)
    $writer.Write([Text.Encoding]::ASCII.GetBytes('data'))
    $writer.Write([int]($sampleCount * 2))
    foreach ($sample in $samples) { $writer.Write([short]$sample) }
} finally { $writer.Dispose() }
