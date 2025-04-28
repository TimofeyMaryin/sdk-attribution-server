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



