# Custom SDK: Back-end and Front-end


## Overview

Данное SDK нужно для полного замещения Appsflyer SDK в области атрибуции данных.

При помощи этого SDK можно полностью организовать аттрибуцию своих данных абсолютно везде: веб сайты, Android/IOS приложения и прочее.

**Сервер работает на фреймворке Ktor (Kotlin):**

_Front-End_ - Ktor DLS

_Back-End_ - Ktor

---

## Архитектура

### Back-end

Архитектура базируется на классах и методах внутри. Безопастное использование сервера гаранитрует **JWT** (JSON Web Token). И абсолютна вся логика связанная с манипуляцией данных внутри проекта защищено JWT ключем.

##### У проекта есть 4 базы данных:

`DatabaseClickPostgresSQL` - База данных подсчета кликов.

`DatabaseEventPostgreSQL` - База данных подсчета ивентов.

`DatabaseImpressionPostgresSQL` - База данных подсчета кликов у приложения (Impression и Click)

`DatabasePostgreSQL` - База данных сохранения установки приложения.

##### У проекта следующие Route-классы

`AdRedirectRoute` - класс предназначен для интеграции Unity ADS.

`AuthRoute` - класс предназначенный для регистрации и входа на сервер.

`ClickRoute` - класс предназначенный для выявления кликов от пользователя с рекламы (Impression/Click).

`DataRoute` - класс связанный с установками и работой с ними.

`EventRoute` - класс предназначенный для работы с Event's в приложении.
 

### Front-end


Later

---

## Модели данных

Здесь собранны главные модели данных в этом проекте. Благодаря правильной организации получается создать рабочий SDK, который позволит граммотно организовать аттрибуцию данных.

### ClickModel

```kotlin
data class ClickModel(
    val id: Int? = null,
    val bundleID: String?,
    val impressionClick: Boolean,
    val click: Boolean
)
```
Модель предназначенная для подсчета кликов. Каждый клик (impression или click) считает как true. Групировка происходит по bundleID, чтоб можно было правильно подсчитать количество кликов для каждого приложения.

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

## Как работает сервер на стороне BackEnd

### Форма входа (yourdomain.com:8081/login)

Пользователь в форме должен ввести корректные данные для входа. После того как он ввел данные по адресу `:8081/login` идет переадресация на `:8081/submit-login`, который получает данные для входа и отправляет **GET** запрос на `:8080/login`. Если все правильно, то сервер отправляет **JWT Token** и сразу идет переадресация на `:8081/admin`.

### Админ панель (yourdomain.com:8081/admin)

Сразу после перехода идет GET запрос на `:8080/apps`, чтоб получить все приложения. После получения все приложений идет группировка по приложениям.

Сегмент с приложением является кликабельным и после клика идет переадресация на `:8081/dashboard/details/{appName}` с qurey запросов в теле которого является название приложения.


### Dashboard приложения (yourdomain.com:8081/dashboard/details/{appName})

Later...

