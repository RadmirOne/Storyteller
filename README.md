# Storyteller

Движок визуальных новелл для Android и iOS на Kotlin Multiplatform и Compose Multiplatform.
Сценарии задаются в JSON: сцены, диалоги, развилки, персонажи, горизонтальная камера и появление фона.

## Структура

- `shared/src/commonMain` — движок, модели, проверка сценариев и общий интерфейс.
- `shared/src/commonMain/composeResources/files/story.json` — встроенная история.
- `androidApp` — Android-приложение.
- `iosApp` — SwiftUI-оболочка общего Compose-интерфейса.
- `shared/src/commonTest` — тесты логики и моделей.
- `shared/src/androidHostTest` — проверка встроенного сценария и изображений.

## Запуск и проверка

Нужны JDK и Android SDK, соответствующие настройкам Gradle проекта. Путь SDK задаётся локально в `local.properties`.

```powershell
.\gradlew.bat :androidApp:assembleDebug
.\gradlew.bat :shared:testAndroidHostTest
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
Для iOS откройте `iosApp/iosApp.xcodeproj` в Xcode на macOS.
Тесты iOS: `./gradlew :shared:iosSimulatorArm64Test` (macOS).

## Возможности и ограничения

При старте проверяются версия формата, ссылки, параметры постановки и загрузка изображений.
Ошибки показываются на экране историй с возможностью повторить загрузку.
Кнопка «К историям» позволяет вернуться и начать прохождение заново.

Состояние хранится в памяти. Сохранения, изменение игровых переменных, звук и редактор пока не реализованы.
Условия выбора проверяются движком, но интерфейс пока показывает все варианты без пояснения недоступности.

## Документация

- [Формат сценария версии 1](docs/story-format.md)
- [Архитектура](docs/context.md)
- [Тестовая история и маршруты проверки](docs/test-story.md)
