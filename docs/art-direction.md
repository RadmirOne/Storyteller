# Художественное направление: «Пока горит маяк»

Выбранный стиль: рисованная кинематографичная новелла. Холодный синий остров и тёплый янтарный свет связывают окружения. Мира и Лев созданы отдельными спрайтами с настоящим alpha-каналом. Изображения созданы встроенным image_gen, без CLI/API. Оригиналы сохранены в Codex; выбранные файлы скопированы в проект. Для аварии добавлены варианты с погасшим главным огнём: lighthouse_room_dark и storm_path_dark.

Размеры фонов — около 1672×941: lighthouse_room имеет 1672×940, lighthouse_room_dark — 1673×940; остальные — 1672×941. Мира — 1024×1536, Лев — 1024×1535. У персонажей сохранена исходная прозрачность. Старые заглушки не используются новым сценарием, оставлены для совместимости с прежними историями. Светлый storm_path сохранён как вариант, активная аварийная сцена использует storm_path_dark.

## Файлы и точные промпты

### wardrobe-scene — прозрачная героиня на сцене

Файл: `shared/src/commonMain/composeResources/files/characters/player/wardrobe-scene.png`. Создан редактированием wardrobe-v2 через встроенный imagegen. Атлас для сцены с прозрачными краями всех девяти ячеек; исходные карточки редактора используют wardrobe-v2. В заголовке диалога портрет больше не дублируется.

```text
Use case background-extraction. Edit this exact 3x3 character wardrobe atlas. Remove ALL the dark teal painted backgrounds completely and replace them with genuine alpha transparency. Keep all nine women, faces, hair, outfits, exact pixel positions, equal 3x3 cell boundaries and square canvas composition unchanged. No other edits. Preserve fine hair edges and opaque clothing. Every pixel outside each woman's silhouette must be transparent including gaps beside arms. NO colored backdrop, NO gradient, NO glow, NO shadow, NO checkerboard painted into the image. Output transparent RGBA PNG. Characters remain framed head to upper thighs exactly as the input, nine separate cutouts in same uniform 3x3 atlas.
```

### Илья и Марк — романтические персонажи

Активный сценарий теперь — короткая романтическая история `lighthouse_weekend`. Мира — подруга героини. Илья и Марк созданы встроенным imagegen как отдельные спрайты с alpha; прозрачность проверена тестами. Старые иллюстрации ниже документируют предыдущую версию истории.

Илья: `shared/src/commonMain/composeResources/files/characters/ilya.png`.

```text
Use case illustration-story. Production character sprite for hand-painted cinematic coastal romantic visual novel. One adult man age 29, Ilya a lighthouse restoration craftsman, handsome thoughtful face, dark wavy hair, subtle stubble, warm brown eyes, restrained kind smile. Rolled-sleeve ivory shirt, charcoal blue work vest, dark trousers, sturdy brown boots. Relaxed standing three-quarter view facing slightly toward viewer, both hands visible naturally at sides. FULL BODY from hair to boots with margins, centered, no cropped feet. Sophisticated digital gouache/oil, realistic anatomy, muted teal shadows and warm amber rim lighting, detailed painterly fabric, matches European animated feature coastal mystery art. Genuine transparent alpha background, no scenery, no floor, no text, no props, no border. Single person only.
```

Марк: `shared/src/commonMain/composeResources/files/characters/mark.png`.

```text
Use case illustration-story. Create ONE full body male character sprite on genuinely TRANSPARENT background, RGBA alpha zero everywhere outside the man, NO glow, NO backdrop, NO gradient, NO ground shadow. Adult man 31, Mark, charming local small-ferry captain, sandy blond short hair swept back, blue-gray eyes, clean shaven, warm easy smile, handsome but natural. Navy pea coat open over muted rust knitted sweater, dark navy trousers, practical dark leather boots. Upright relaxed standing three-quarter pose looking toward viewer, arms resting naturally at sides, no hat, no props. Entire body head to soles with small margins. Painterly digital oil/gouache cinematic visual novel illustration, natural anatomy, soft cool teal shadows warm amber highlights ONLY ON THE PERSON, sophisticated illustrated coastal romance aesthetic. Portrait 2:3. No text or borders. Alpha cutout production sprite.
```

### wardrobe-v2 — выровненная примерка

Активный файл: `shared/src/commonMain/composeResources/files/characters/player/wardrobe-v2.png`. Редактирование исходного атласа встроенным imagegen: единая поза с опущенными руками и согласованные лица и причёски. Исходный атлас сохранён. Это нарисованные варианты, а не послойная модель: абсолютное пиксельное совпадение всех деталей между нарядами не гарантируется.

```text
Edit target: supplied 3x3 wardrobe atlas for a visual novel. Preserve exact equal 3x3 grid, no borders or gutters, square image. Rows blonde, brunette, redhead; columns teal jacket, plum sweater, olive trench. Correct identity and pose drift: within EACH row the three women must have EXACTLY the same head, facial expression, hairstyle strand placement, head position, neck, body proportions, and arm pose. Use each row's leftmost woman as the immutable identity reference. Both arms hang relaxed at sides in ALL cells; no hands in pockets. Only fabric, garment silhouette, collar and seams change. Same camera scale head to upper thighs. Keep beautiful painterly style and identical dark teal background and lighting. Copy the same face/hair across each row rather than reinterpreting it. Distinct tailored jacket, textured knit sweater, belted trench with clear lapels. No text. Production dress-up atlas, absolutely aligned subjects.
```

### wardrobe — выбор героини и одежды

Файл: `shared/src/commonMain/composeResources/files/characters/player/wardrobe.png`. Создан встроенным image_gen, без CLI/API. Единый атлас 3×3 для девяти сочетаний внешности и одежды.

```text
Use case: illustration-story. Create ONE production character selection atlas image for a cinematic hand-painted coastal mystery visual novel. Exact uniform 3 columns by 3 rows grid, nine equal rectangular cells, no gutters, no borders, no text. Overall square image. Each cell shows one adult female protagonist from head to upper thighs, centered, entire head with generous headroom and arms within its cell, same scale and relaxed pose. ROW 1 same blonde woman with wavy shoulder-length golden hair and blue eyes repeated three times. ROW 2 same brunette woman with long dark brown hair, olive skin and brown eyes repeated three times. ROW 3 same copper red-haired woman with freckles, loose braid and green eyes repeated three times. COLUMN 1 each woman wears a tailored deep teal short jacket over ivory top and dark trousers. COLUMN 2 each woman wears a textured plum cable knit sweater and dark trousers. COLUMN 3 each woman wears an olive belted trench coat with lapels. Preserve each woman's exact facial identity and hairstyle across her row, only clothing changes. Beautiful detailed expressive faces, natural adult proportions, sophisticated digital gouache/oil illustration, soft warm amber rim light, cool blue shadows, muted dark teal studio backdrop identical in every cell. Premium visual novel art, not cartoon icons, not photorealistic. No accessories crossing cell edges. This image will be used directly as a 3x3 texture atlas; grid cells must be exactly equal.
```

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

## Эмоции демоглавы (30 сентября 2026)

Через встроенный ImageGen созданы два дополнительных спрайта; исходные изображения не заменены:

- `shared/src/commonMain/composeResources/files/characters/ilya-worried.png` — тревога при остановке механизма.
- `shared/src/commonMain/composeResources/files/characters/mark-laughing.png` — тёплый смех в разговоре о совместной работе.

Промпт Ильи: «Use case: identity-preserve. Edit target: Ilya, adult visual novel character in attached image. Produce one full-body emotional variant: visibly worried, brows drawn together, closed unsmiling mouth, worried eyes. Keep identity, exact dark wavy hair, vest, shirt, trousers, boots, pose, proportions, painterly realistic style and framing unchanged. Entire character visible, transparent background, no glow, no text. This is a production sprite for the same game.» Референс — `files/characters/ilya.png`.

Промпт Марка: «Use case: identity-preserve. Edit target Mark from reference, adult visual novel character. One full-body emotional variant with a sincere delighted laugh, eyes smiling, naturally open smile. Keep identity, blond hair, blue peacoat, red sweater, trousers, boots, pose and proportions, same painterly realistic style and entire full-body framing. Transparent background; no glow, no text. Production game sprite.» Референс — `files/characters/mark.png`.

Эмоция выбирается на уровне реплики через `speakerSpriteResource`, без дублирования персонажей в списке действующих лиц. Alpha-канал сохранён. Финальную читаемость эмоций на маленьких экранах следует проверить на устройстве.
