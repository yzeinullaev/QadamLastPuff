# Qadam Last Puff

Android-приложение для отказа от курения с акцентом на SOS-режим при сильной тяге.

## Стек

- Kotlin
- Jetpack Compose + Material 3
- Room
- DataStore
- Navigation Compose
- WorkManager (локальные уведомления)

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

Откройте проект в Android Studio и запустите на эмуляторе или устройстве (API 26+).

```bash
./gradlew assembleDebug
```

## Структура

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
