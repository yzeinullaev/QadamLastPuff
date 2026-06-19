# Qadam Last Puff

Приложение для отказа от курения с акцентом на SOS-режим при сильной тяге.

Доступны две платформы:
- **Android** — Kotlin, Jetpack Compose
- **iOS** — Swift, SwiftUI

## Стек

### Android
- Kotlin
- Jetpack Compose + Material 3
- Room
- DataStore
- Navigation Compose
- WorkManager (локальные уведомления)

### iOS
- Swift 5
- SwiftUI
- JSON-хранилище + UserDefaults
- UNUserNotificationCenter (локальные уведомления)

## Экраны

1. **Onboarding** — настройка профиля, причин и SOS-контакта
2. **Главная** — статистика, мотивация, кнопка SOS
3. **SOS-режим** — таймер 3 мин, действия, экстренный режим
4. **Прогресс** — детальная аналитика
5. **Деньги и цель** — финансовая мотивация
6. **Здоровье** — timeline восстановления
7. **Достижения** — награды за прогресс
8. **Профиль** — редактирование и настройки

## Сборка

### Android

Откройте проект в Android Studio и запустите на эмуляторе или устройстве (API 26+).

```bash
./gradlew assembleDebug
```

### iOS

Откройте `ios/QadamLastPuff.xcodeproj` в Xcode, выберите Team в Signing & Capabilities и запустите на симуляторе или устройстве (iOS 17+).

```bash
cd ios
xcodebuild -project QadamLastPuff.xcodeproj -scheme QadamLastPuff -destination 'platform=iOS Simulator,name=iPhone 16' build
```

## Структура

### Android
```
app/src/main/java/com/qadam/lastpuff/
├── data/           # Room, DataStore, репозитории
├── domain/model/   # Модели данных
├── ui/
│   ├── screens/    # Экраны
│   ├── navigation/ # Навигация
│   ├── theme/      # Тема Material 3
│   └── viewmodel/  # ViewModel
├── util/           # Константы и расчёты
└── worker/         # WorkManager уведомления
```

### iOS
```
ios/QadamLastPuff/
├── Data/           # DataStore, UserDefaults, репозиторий
├── Domain/         # SOS-сообщения и логика поддержки
├── Models/         # Модели данных
├── Utils/          # Константы и расчёты
├── ViewModels/     # AppViewModel
├── Views/
│   ├── Screens/    # Экраны
│   ├── Components/ # UI-компоненты
│   └── Navigation/ # ContentView, TabView
└── Theme/          # Цвета и тема
```
