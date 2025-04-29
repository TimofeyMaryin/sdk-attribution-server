# Custom Attribution SDK — Back-end & Front-end (Ktor)


## Overview

Данное SDK нужно для полного замещения Appsflyer SDK в области атрибуции данных.

Данный SDK позволяет организовать независимую систему атрибуции на всех платформах: веб, Android, iOS и другие.

**Сервер работает на фреймворке Ktor (Kotlin):**

_Front-End_ - Ktor DLS

_Back-End_ - Ktor

---

## Архитектура

### Back-end

Архитектура базируется на классах и методах внутри. Безопасное использование сервера гаранитрует **JWT** (JSON Web Token). Вся логика обработки данных защищена через JWT-авторизацию, что гарантирует безопасность и контроль доступа.

#### Проект использует четыре отдельные базы данных (PostgreSQL):

- `sdk_server_click` — хранение информации о кликах.


- `sdk_server_event` — данные по ивентам внутри приложений.


- `sdk_server_impression` — трекинг показов и кликов рекламы.


- `sdk_server` — установка приложений и общие данные.

##### У проекта следующие Route-классы


| Route           | Назначение                                      |
|----------------|--------------------------------------------------|
| `AdRedirectRoute` | Интеграция с Unity Ads                         |
| `AuthRoute`     | Регистрация и вход                              |
| `ClickRoute`    | Отслеживание кликов и показов                   |
| `DataRoute`     | Работа с установками                            |
| `EventRoute`    | Обработка пользовательских событий (Events)     |


### Front-end

_Раздел в разработке..._

- [ ] Добавить архитектуру фронта
- [ ] UI-структура: формы, роутинг, логика
- [ ] Технические детали (стек, шаблонизатор, CSS-фреймворк)


---

## Модели данных

Здесь собранны главные модели данных в этом проекте. Благодаря правильной организации получается создать рабочий SDK, который позволит грамотно организовать аттрибуцию данных.

### ClickModel

```kotlin
data class ClickModel(
    val id: Int? = null,
    val bundleID: String?,
    val impressionClick: Boolean,
    val click: Boolean
)
```
Модель для фиксации кликов:

- `bundleID` — уникальный идентификатор приложения.
- `impressionClick` — был ли показ рекламы.
- `click` — был ли клик.


### EventModel

```kotlin
@Serializable
data class EventData(
    val event: String,
    val bundleId: String,
    val deviceId: String,
    val id: Int? = null,
)
```
Класс для подсчтеа ивентов.
- `event` - название ивента
- `bundleId` - bundleID приложения
- `deviceId` - ID телефона с которого ивент получаю

### InstallData

```kotlin
@Serializable
data class InstallData(
    val bundleId: String,
    val appName: String,
    val appVersion: String,
    val deviceId: String,
    val deviceModel: String,
    val deviceManufacturer: String,
    val androidVersion: String,
    val apiLevel: Int,
    val language: String,
    val country: String,
    val installReferrer: String? = null,
    val isFirstInstall: Boolean,
    val googleAdId: String? = null,
    val networkType: String,
    val isFromPlayStore: Boolean,
    val timestamp: Long = Instant.now().toEpochMilli(),
    val unityAdsData: String? = null,
    val id: Int? = null,
    val utmData: String? = null,
    val event: String? = null,
)
```

Главный класс для подсчета установок, где собираются основные данные о приложении и пользователе.



---

## Configuration

#### Настройка проекта

1. Скачать PostgresSQL
2. Создать базы данных:


```sql
CREATE DATABASE sdk_server_click;
CREATE DATABASE sdk_server_event;
CREATE DATABASE sdk_server_impression;
CREATE DATABASE sdk_server;
```

3. Настроить порты:

- Освободить порты :8080 (Back-End) и :8081 (Front-end)
- В конфигурации проекта изменить порты на свои

4. Запустить проект 🏆

---

## Как работает сервер на стороне Front-end

### Форма входа (yourdomain.com:8081/login)

Пользователь в форме должен ввести корректные данные для входа. После того как он ввел данные по адресу `:8081/login` идет переадресация на `:8081/submit-login`, который получает данные для входа и отправляет **GET** запрос на `:8080/login`. Если все правильно, то сервер отправляет **JWT Token** и сразу идет переадресация на `:8081/admin`.

### Админ панель (yourdomain.com:8081/admin)

Сразу после перехода идет GET запрос на `:8080/apps`, чтоб получить все приложения. После получения все приложений идет группировка по приложениям.

Сегмент с приложением является кликабельным и после клика идет переадресация на `:8081/dashboard/details/{appName}` с query запросов в теле которого является название приложения.


### Dashboard приложения (yourdomain.com:8081/dashboard/details/{appName})

Later...

