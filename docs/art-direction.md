# Художественное направление: «Пока горит маяк»

Выбранный стиль: рисованная кинематографичная новелла. Холодный синий остров и тёплый янтарный свет связывают окружения. Мира и Лев созданы отдельными спрайтами с настоящим alpha-каналом. Изображения созданы встроенным image_gen, без CLI/API. Оригиналы сохранены в Codex; выбранные файлы скопированы в проект. Для аварии добавлены варианты с погасшим главным огнём: lighthouse_room_dark и storm_path_dark.

Размеры фонов — около 1672×941: lighthouse_room имеет 1672×940, lighthouse_room_dark — 1673×940; остальные — 1672×941. Мира — 1024×1536, Лев — 1024×1535. У персонажей сохранена исходная прозрачность. Старые заглушки не используются новым сценарием, оставлены для совместимости с прежними историями. Светлый storm_path сохранён как вариант, активная аварийная сцена использует storm_path_dark.

## Файлы и точные промпты

### lighthouse_room_dark

Файл: `shared/src/commonMain/composeResources/files/scenes/lighthouse_room_dark.png`

```text
Edit target: this exact lighthouse room background. Change ONLY lighting: the central Fresnel lens and the main lighthouse lamp are now completely switched OFF after an electrical failure, the lens is dark glass with faint blue reflections, no glowing core and no beam. Small existing peripheral emergency lanterns still give restrained warm amber pools; moonlit storm-blue windows provide enough illumination for the room to remain readable. Keep architecture, camera perspective, object geometry, room layout, image dimensions, open floor and hand-painted cinematic style exactly unchanged. Do not add anything, no people, no text, no UI. Output a matching dark-state background for the same game scene.
```

### storm_path_dark

Файл: `shared/src/commonMain/composeResources/files/scenes/storm_path_dark.png`

```text
Edit target: this exact painted storm-path landscape. Change only the distant lighthouse main lantern at the top of the tower: turn its light completely off, dark blue-gray glass, NO beam shining from the lighthouse. Keep the foreground amber hurricane lamp lit, all other scene geometry, stone ground, sea spray, rain, composition, aspect ratio, colors and painted detail exactly unchanged. No people, no text, no extra objects. This is the same scene during a lighthouse power failure.
```


### island_arrival

Файл: `shared/src/commonMain/composeResources/files/scenes/island_arrival.png`

```text
Use case: illustration-story. Asset: production background for a hand-painted cinematic visual novel 'While the Lighthouse Burns'. Create one expansive landscape 16:9 coastal island arrival scene at blue hour before a storm. Eye-level view across a broad wet stone quay, distant slate sea, a modest ferry moored to the far left, a winding path and white weathered lighthouse on the far right, island cottages between. Rich painterly gouache and digital oil detail, sophisticated European animated feature mood, restrained teal blue and warm amber lamps, luminous atmospheric depth, credible natural perspective, crisp material textures. Composition for horizontal camera panning in a portrait mobile viewport: continuous environment with interesting left middle right thirds; unobstructed walkable stone ground across entire lower 30 percent for separately composited standing characters, horizon roughly 40 percent from top. No people, no silhouettes, no lettering, no watermark, no panels, no UI. Beautiful finished illustration rather than sketch.
```

### lighthouse_room

Файл: `shared/src/commonMain/composeResources/files/scenes/lighthouse_room.png`

```text
Use case illustration-story. Create ONE NEW finished landscape 16:9 visual novel background. Same hand-painted cinematic digital oil/gouache aesthetic, restrained teal-blue shadows and warm amber light as an atmospheric island story. Interior of an old working lighthouse signal room at stormy night: tall curved windows overlooking the sea in the back, brass radio and chart table to far left, a large impressive Fresnel lens and hand-crank mechanism at rear center, weathered stairs and lamp rack at far right, human-scale architecture. Beautiful nuanced paint texture, cinematic lighting, credible perspective, not photorealistic. Wide continuous room for horizontal mobile camera pan. Eye level, lower 30% open unobstructed wooden floor for separate standing sprites at x=.32 and .68. No people, no silhouettes, no text, no UI, no watermark.
```

### storm_path

Файл: `shared/src/commonMain/composeResources/files/scenes/storm_path.png`

```text
Use case illustration-story. Create ONE NEW finished landscape 16:9 visual novel background. Cinematic hand-painted digital oil/gouache, detailed elegant animated feature scenery, teal-blue storm and warm amber accents. A storm-battered island lighthouse approach at night: broad stone terrace and safe stone causeway across rocky coastline, distant lighthouse glowing at upper right, violently foaming sea below parapet and jagged rocks, blown spray and slanting rain, an amber hurricane lamp in far left niche. Human eye-level from terrace. Broad continuous walking ground across lower third; keep x=.32 and .68 clear for separately layered standing character sprites. Dramatic yet legible, no people, no silhouettes, no boats in foreground, no text, no UI, no panels, no watermark.
```

### harbor_dawn

Файл: `shared/src/commonMain/composeResources/files/scenes/harbor_dawn.png`

```text
Use case illustration-story. Create ONE NEW finished landscape 16:9 visual novel background. Cinematic hand-painted digital oil/gouache aesthetic with subtle painterly edges and atmospheric depth. Quiet island fishing harbor after the storm at dawn: broad wet stone quay in foreground, small rescue boat and repaired fishing vessel at far left beyond quay, white weathered lighthouse on rocky hill at far right, cottages, soft apricot sunrise breaking through pale blue clouds, calm reflective water, small coils of rope at edges. Hopeful restrained mood, amber and blue visual continuity. Eye-level broad panoramic composition for horizontal camera; unobstructed stone ground across lower 30% for separately composited standing sprites. No humans, no silhouettes, no lettering, no UI, no watermark.
```

### mira

Файл: `shared/src/commonMain/composeResources/files/characters/mira.png`

```text
Use case illustration-story. Production full-body character sprite for a sophisticated hand-painted cinematic visual novel about an island lighthouse. ONE woman only: Mira, 28, expressive thoughtful face, subtle freckles, copper-red hair braided over shoulder, practical dark teal-blue raincoat open over knit sweater, muted amber scarf, dark trousers, worn brown ankle boots. Human natural adult proportions, face clearly detailed and appealing, restrained worried but warm expression, three-quarter view almost frontal, weight relaxed on one leg, hands naturally relaxed visible, no objects held. Elegant painterly digital oil/gouache shading, crisp clean silhouette, soft blue ambient light and subtle warm amber rim light, detailed fabric, not photorealism, not chibi. Portrait 2:3 framing entire body including shoes, head near top and boots near bottom with only small padding, character centered horizontally. GENUINELY TRANSPARENT alpha background; no scenery, no floor, no painted checkerboard, no cast shadow rectangle, no text, no watermark, no extra characters. PNG cutout ready to composite over scenery.
```

### lev

Файл: `shared/src/commonMain/composeResources/files/characters/lev.png`

```text
Use case illustration-story. Production full-body character sprite for a sophisticated hand-painted cinematic visual novel about an island lighthouse. ONE man only: Lev, a 55-year-old lighthouse keeper, short windswept silver hair, close-cropped salt-and-pepper beard, lined kindly tired face, ochre yellow weathered raincoat open over dark navy fisherman sweater, charcoal work trousers, sturdy worn black boots. Natural adult proportions, expressive face detailed, sturdy build, calm serious expression, three-quarter view almost frontal, neutral standing pose, arms comfortably resting at sides hands visible, no objects held. Elegant painterly digital oil/gouache shading, crisp clean silhouette, soft blue ambient light and subtle warm amber rim light, detailed worn fabric, not photorealism, not chibi. Portrait 2:3 framing entire body including shoes, head near top and boots near bottom with only small padding, character centered horizontally. GENUINELY TRANSPARENT alpha background; no scenery, no floor, no painted checkerboard, no rectangular cast shadow, no text, no watermark, no extra characters. PNG cutout ready to composite over scenery.
```
