# TouchEmias

**[English below](#english)**

---

## Русский

### О проекте

TouchEmias — Android-приложение для автоматического мониторинга доступных талонов на приём к врачу через портал ЕМИАС (emias.info). Приложение проверяет расписание в фоновом режиме и уведомляет пользователя о появлении свободного слота, а в режиме автозаписи — записывает автоматически.

### Возможности

- **Авторизация через mos.ru** — вход через СУДИР OAuth (единый портал госуслуг Москвы)
- **Выбор специальности и врача** — поиск по всем доступным специальностям и врачам вашей поликлиники
- **Настройка мониторинга** — выбор дат, временного диапазона и интервала проверки (от 30 секунд до 1 часа)
- **Режим уведомления** — push-уведомление при появлении свободного талона
- **Режим автозаписи** — автоматическая запись на приём без участия пользователя
- **Работа в фоне** — мониторинг продолжается после закрытия приложения через Foreground Service
- **Автозапуск** — возобновление мониторинга после перезагрузки устройства
- **Журнал событий** — история проверок и действий сервиса

### Технологии

| Слой | Технологии |
|---|---|
| UI | Jetpack Compose, Material3, Navigation Compose |
| Архитектура | MVVM, Hilt (DI), ViewModel, StateFlow |
| Сеть | Retrofit, OkHttp, Gson |
| Авторизация | WebView в нативном Activity, XHR-интерсептор на JavaScript |
| Хранилище | Room (база данных), DataStore (настройки) |
| Фоновая работа | Foreground Service, BroadcastReceiver (BOOT_COMPLETED) |

### Как это работает

#### Авторизация
1. Приложение открывает страницу входа `login.mos.ru` в нативном `WebView Activity`
2. После входа СУДИР перенаправляет браузер на `emias.info/sudir-web`
3. Страница ЕМИАС делает XHR-запрос `whoAmI` с токеном доступа в теле запроса
4. JavaScript-интерсептор перехватывает токен и передаёт его в приложение
5. Токен сохраняется и используется для всех последующих запросов к API ЕМИАС

#### Мониторинг
1. Пользователь выбирает специальность → врача → даты и время → интервал проверки → режим
2. Запускается Foreground Service, который периодически опрашивает API ЕМИАС
3. При обнаружении свободного слота:
   - **NOTIFY_ONLY**: отправляется push-уведомление с деталями талона
   - **AUTO_BOOK**: приложение автоматически записывает на приём и уведомляет об успехе

### Требования

- Android 10 (API 29) и выше
- Полис ОМС московского фонда
- Прикрепление к московской поликлинике в системе ЕМИАС

### Сборка

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

APK окажется в `app/build/outputs/apk/debug/app-debug.apk`.

### Структура проекта

```
app/src/main/java/ru/touchemiasapp/
├── data/
│   ├── api/          # Retrofit-интерфейсы и DTO (ЕМИАС API v2)
│   ├── auth/         # Хранение токена СУДИР (DataStore)
│   ├── db/           # Room: задания мониторинга, журнал событий
│   ├── preferences/  # Настройки пользователя (ОМС, дата рождения)
│   └── repository/   # Реализации репозиториев
├── domain/
│   ├── model/        # Доменные модели (Doctor, TimeSlot, WatchConfig…)
│   └── repository/   # Интерфейсы репозиториев
├── service/
│   ├── MonitorService.kt   # Foreground Service — цикл мониторинга
│   └── BootReceiver.kt     # Автозапуск после перезагрузки
└── ui/
    ├── auth/         # Экран входа (WebView Activity + Compose)
    ├── specialities/ # Список специальностей
    ├── doctors/      # Список врачей с расписанием
    ├── schedule/     # Настройка параметров мониторинга
    ├── monitor/      # Главный экран — статус и управление
    └── logs/         # Журнал событий
```

### Важные технические детали

**WebView на Samsung Galaxy (Android 15)**
На устройствах Samsung с Android 15 и Chrome WebView 150+ страница `login.mos.ru` не отображалась при встраивании WebView в Jetpack Compose — из-за конфликта SurfaceControl-рендеринга с Compose-пайплайном. Решение: WebView вынесен в отдельный нативный `Activity` без Compose (`LoginWebViewActivity`).

**API ЕМИАС v2**
Приложение использует внутренний API портала emias.info. Все запросы отправляются на `https://emias.info/api/new/` с токеном авторизации в заголовке. Структура ответов: `{ payload: ..., error: { code, description } }`.

### Дисклеймер

Это неофициальный клиент. Приложение использует внутренний API портала ЕМИАС, который не документирован публично и может измениться в любой момент. Используйте на свой страх и риск.

---

## English

### About

TouchEmias is an Android app for automated monitoring of available doctor appointment slots on the EMIAS portal (emias.info — Moscow's unified medical information and analysis system). It polls the schedule in the background and notifies the user when a slot becomes available, and can automatically book it.

### Features

- **Login via mos.ru** — authentication through SUDIR OAuth (Moscow government services portal)
- **Specialty and doctor selection** — browse all available specialties and doctors at your clinic
- **Configurable monitoring** — choose dates, time range, and polling interval (30 seconds to 1 hour)
- **Notification mode** — push notification when a free appointment slot appears
- **Auto-book mode** — automatically books the appointment without user interaction
- **Background operation** — monitoring continues after the app is closed via a Foreground Service
- **Auto-start** — resumes monitoring after device reboot
- **Event log** — history of checks and service actions

### Tech stack

| Layer | Technologies |
|---|---|
| UI | Jetpack Compose, Material3, Navigation Compose |
| Architecture | MVVM, Hilt (DI), ViewModel, StateFlow |
| Network | Retrofit, OkHttp, Gson |
| Auth | Native Activity WebView, JavaScript XHR interceptor |
| Storage | Room (database), DataStore (preferences) |
| Background | Foreground Service, BroadcastReceiver (BOOT_COMPLETED) |

### How it works

#### Authentication
1. The app opens the `login.mos.ru` login page in a native `WebView Activity`
2. After login, SUDIR redirects the browser to `emias.info/sudir-web`
3. The EMIAS SPA makes a `whoAmI` XHR request with an access token in the request body
4. A JavaScript interceptor captures the token and passes it to the app
5. The token is stored and used for all subsequent EMIAS API requests

#### Monitoring
1. User selects specialty → doctor → dates and time range → polling interval → mode
2. A Foreground Service starts and periodically polls the EMIAS API
3. When a free slot is found:
   - **NOTIFY_ONLY**: sends a push notification with the slot details
   - **AUTO_BOOK**: automatically books the appointment and notifies the user

### Requirements

- Android 10 (API 29) or higher
- Moscow OMS (compulsory health insurance) policy
- Registration at a Moscow clinic in the EMIAS system

### Build

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Project structure

```
app/src/main/java/ru/touchemiasapp/
├── data/
│   ├── api/          # Retrofit interfaces and DTOs (EMIAS API v2)
│   ├── auth/         # SUDIR token storage (DataStore)
│   ├── db/           # Room: monitoring jobs, event log
│   ├── preferences/  # User preferences (OMS number, birth date)
│   └── repository/   # Repository implementations
├── domain/
│   ├── model/        # Domain models (Doctor, TimeSlot, WatchConfig…)
│   └── repository/   # Repository interfaces
├── service/
│   ├── MonitorService.kt   # Foreground Service — monitoring loop
│   └── BootReceiver.kt     # Auto-start on device reboot
└── ui/
    ├── auth/         # Login screen (WebView Activity + Compose)
    ├── specialities/ # Specialty list
    ├── doctors/      # Doctor list with schedule
    ├── schedule/     # Monitoring parameters setup
    ├── monitor/      # Main screen — status and controls
    └── logs/         # Event log
```

### Notable technical details

**WebView on Samsung Galaxy (Android 15)**
On Samsung Galaxy devices with Android 15 and Chrome WebView 150+, the `login.mos.ru` page was blank when the WebView was embedded in Jetpack Compose — due to a conflict between SurfaceControl rendering and the Compose pipeline. The fix: WebView is moved to a standalone native `Activity` without Compose (`LoginWebViewActivity`).

**EMIAS API v2**
The app uses the internal API of the emias.info portal. All requests go to `https://emias.info/api/new/` with an authorization token in the header. Response structure: `{ payload: ..., error: { code, description } }`.

### Disclaimer

This is an unofficial client. The app uses the internal EMIAS API which is not publicly documented and may change at any time. Use at your own risk.
