# TouchEmias

**[English below](#english)**

---

## Русский

### О проекте

TouchEmias — Android-приложение для автоматического мониторинга доступных талонов на приём к врачу через портал ЕМИАС (emias.info). Приложение проверяет расписание в фоновом режиме и уведомляет пользователя о появлении свободного слота, а в режиме автозаписи — записывает автоматически.

Сервис мониторинга работает **полностью локально, на самом телефоне** — никакого выделенного сервера или облачного бэкенда нет. Приложение само периодически опрашивает API ЕМИАС из фонового `Foreground Service` на устройстве пользователя, поэтому для стабильной работы ему требуется ряд системных разрешений (см. раздел [«Требуемые разрешения»](#требуемые-разрешения)).

### Возможности

- **Авторизация через mos.ru** — вход через СУДИР OAuth (единый портал госуслуг Москвы)
- **Выбор специальности и врача** — поиск по всем доступным специальностям и врачам вашей поликлиники
- **Настройка мониторинга** — выбор дат, временного диапазона и интервала проверки (от 30 секунд до 1 часа)
- **Режим уведомления** — push-уведомление при появлении свободного талона
- **Режим автозаписи** — автоматическая запись на приём без участия пользователя
- **Работа в фоне** — мониторинг продолжается после закрытия приложения через Foreground Service
- **Автозапуск** — возобновление мониторинга после перезагрузки устройства
- **Журнал событий** — история проверок и действий сервиса

### Как пользоваться

1. **Авторизация.** При первом запуске приложение открывает страницу входа `login.mos.ru` во встроенном `WebView`. Войдите под своей учётной записью mos.ru (СУДИР) — так же, как на сайте госуслуг. После успешного входа приложение перехватывает токен доступа и переходит к следующему шагу.
2. **Данные полиса.** Введите номер полиса ОМС и дату рождения — по этим данным ЕМИАС ищет ваше прикрепление к поликлинике и доступных врачей. Эти данные хранятся только на устройстве.
3. **Выбор специальности и врача.** Выберите нужную специальность из списка, затем — конкретного врача. Для каждого врача показывается ближайшее доступное расписание.
4. **Настройка мониторинга.** На экране настройки задаются параметры отслеживания:
   - **Желаемые даты** — один или несколько дней из ближайших 30, в которые вас устроит приём (кнопки с датами, например «Пн, 27 июл.»).
   - **Временной диапазон** — время «с» и «по», в которое должен попадать талон (например, чтобы отфильтровать только утренние приёмы).
   - **Режим работы** — «Уведомление» (прислать push, когда появится слот) или «Автозапись» (записать на приём автоматически, без подтверждения).
   - **Интервал проверки** — как часто опрашивать расписание: от 30 секунд до 1 часа. Чем короче интервал, тем выше шанс поймать талон первым, но и тем активнее расходуется батарея.
5. **Запуск и контроль.** После нажатия «Начать мониторинг» запускается фоновый сервис. На главном экране отображается статус мониторинга, его можно остановить в любой момент. В разделе «Журнал» видна история всех проверок и найденных/забронированных талонов.

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

### Требуемые разрешения

Так как мониторинг работает локально на телефоне (см. выше), приложению нужны системные разрешения для стабильной фоновой работы. Разрешения, которые система может запросить у пользователя явно (например, показ уведомлений), запрашиваются **только в момент, когда они реально нужны** — например, право на уведомления запрашивается не при установке, а только при нажатии кнопки «Начать мониторинг» в режиме уведомлений.

| Разрешение | Зачем нужно |
|---|---|
| `INTERNET` | Обращение к API портала ЕМИАС и к странице авторизации mos.ru |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC` | Позволяют сервису мониторинга работать в фоне и не быть остановленным системой во время проверки расписания |
| `POST_NOTIFICATIONS` (Android 13+) | Показ push-уведомления о найденном свободном талоне в режиме «Уведомление». Запрашивается у пользователя только при старте мониторинга |
| `WAKE_LOCK` | Не даёт устройству уходить в глубокий сон между проверками расписания, пока мониторинг активен |
| `SCHEDULE_EXACT_ALARM` | Точное соблюдение выбранного интервала проверки (от 30 секунд до 1 часа) |
| `RECEIVE_BOOT_COMPLETED` | Автоматическое возобновление мониторинга после перезагрузки устройства, но только если на момент перезагрузки был запущен активный мониторинг |

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

The monitoring service runs **entirely locally, on the phone itself** — there is no dedicated server or cloud backend. The app periodically polls the EMIAS API from a background `Foreground Service` on the user's device, which is why it needs a number of system permissions to work reliably (see [Required permissions](#required-permissions)).

### Features

- **Login via mos.ru** — authentication through SUDIR OAuth (Moscow government services portal)
- **Specialty and doctor selection** — browse all available specialties and doctors at your clinic
- **Configurable monitoring** — choose dates, time range, and polling interval (30 seconds to 1 hour)
- **Notification mode** — push notification when a free appointment slot appears
- **Auto-book mode** — automatically books the appointment without user interaction
- **Background operation** — monitoring continues after the app is closed via a Foreground Service
- **Auto-start** — resumes monitoring after device reboot
- **Event log** — history of checks and service actions

### How to use

1. **Sign in.** On first launch, the app opens the `login.mos.ru` login page in an embedded `WebView`. Log in with your mos.ru (SUDIR) account — same as on the government services website. Once login succeeds, the app captures the access token and moves to the next step.
2. **Policy details.** Enter your OMS (compulsory health insurance) policy number and date of birth — EMIAS uses this to look up your clinic registration and available doctors. This data is stored on the device only.
3. **Specialty and doctor selection.** Pick the specialty you need from the list, then a specific doctor. Each doctor's card shows their nearest available schedule.
4. **Monitoring setup.** The setup screen lets you configure:
   - **Desired dates** — one or more days within the next 30 that would work for you (date buttons, e.g. "Mon, Jul 27").
   - **Time range** — a "from" and "to" time the slot must fall within (e.g. to only catch morning appointments).
   - **Mode** — "Notify" (send a push notification when a slot appears) or "Auto-book" (book the appointment automatically, without confirmation).
   - **Polling interval** — how often to check the schedule: from 30 seconds to 1 hour. A shorter interval increases the chance of catching a slot first but uses more battery.
5. **Start and control.** Tapping "Start monitoring" launches the background service. The main screen shows the monitoring status and lets you stop it at any time. The "Logs" screen shows the history of every check and every slot found or booked.

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

### Required permissions

Since monitoring runs locally on the phone (see above), the app needs system permissions to keep working reliably in the background. Permissions that the system can prompt the user for explicitly (e.g. notifications) are requested **only at the moment they're actually needed** — for example, the notification permission is not requested at install time, only when the user taps "Start monitoring" in notification mode.

| Permission | Why it's needed |
|---|---|
| `INTERNET` | Talking to the EMIAS API and the mos.ru login page |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC` | Let the monitoring service keep running in the background and avoid being killed by the system while checking the schedule |
| `POST_NOTIFICATIONS` (Android 13+) | Shows a push notification when a free slot is found in "Notify" mode. Requested from the user only when monitoring is started |
| `WAKE_LOCK` | Prevents the device from going into deep sleep between schedule checks while monitoring is active |
| `SCHEDULE_EXACT_ALARM` | Keeps the chosen polling interval accurate (from 30 seconds to 1 hour) |
| `RECEIVE_BOOT_COMPLETED` | Automatically resumes monitoring after a device reboot, but only if monitoring was actively running at the time of the reboot |

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
