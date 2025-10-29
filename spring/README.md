# Система бронирования номеров (Hotel Booking System)

Микросервисная архитектура для управления бронированием номеров в отелях с распределенной обработкой запросов и обеспечением согласованности данных между сервисами.

## 📋 Содержание

- [Технологический стек](#технологический-стек)
- [Архитектура системы](#архитектура-системы)
- [Структура проекта](#структура-проекта)
- [Инструкция по запуску](#инструкция-по-запуску)
- [API Endpoints](#api-endpoints)
- [ADR (Architecture Decision Records)](#adr-architecture-decision-records)
- [Особенности реализации](#особенности-реализации)

## 🛠 Технологический стек

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Cloud 2025.0.0**
  - Spring Cloud Gateway (маршрутизация)
  - Netflix Eureka (service discovery)
  - OpenFeign (межсервисная коммуникация)
  - Spring Retry (повторные попытки)
- **Spring Security** (JWT аутентификация)
- **JPA / Hibernate** (ORM)
- **Flyway** (миграции БД)
- **H2 Database** (in-memory БД)
- **Maven** (сборка проекта)
- **Lombok** (упрощение кода)

## 🏗 Архитектура системы

### Схема компонентов

```
┌─────────────────────────────────────────────────────────────────┐
│                        Gateway Service                           │
│                      (Port: 8080)                               │
│              Spring Cloud Gateway + CORS                        │
└────────────────────┬────────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
┌───────────┐  ┌───────────┐  ┌───────────┐
│   Auth    │  │  Booking  │  │   Hotel   │
│  Service  │  │  Service  │  │  Service  │
│  :8084    │  │  :8083    │  │  :8085    │
└─────┬─────┘  └─────┬─────┘  └─────┬─────┘
      │              │              │
      │              │              │
      └──────────────┼──────────────┘
                     │
                     ▼
            ┌────────────────┐
            │  Eureka Server │
            │    :8761       │
            └────────────────┘
```

### Поток запроса бронирования

```
Client → Gateway → Booking Service
                    ├─→ Auth Service (получение CustomerDetails)
                    └─→ Hotel Service
                        ├─→ resolveAvailableRoomId (выбор номера)
                        └─→ incrementRoomUsage (увеличение счетчика)
```

### Модель данных

**auth-service:**
- `user_access` - учетные записи пользователей (email, password, role, userId)

**booking-service:**
- `users` - пользователи (firstName, lastName, email, login)
- `bookings` - бронирования (roomId, startDate, endDate, status, rqId, version)

**hotel-service:**
- `hotels` - отели (name, address)
- `rooms` - номера (hotelId, number, available, timesBooked, version)
- `processed_requests` - обработанные запросы для идемпотентности (requestId, roomId, operationType)

## 📁 Структура проекта

```
spring/
├── auth/                    # Сервис аутентификации и авторизации
│   ├── controllers/        # REST контроллеры
│   ├── security/           # JWT, Security конфигурация
│   ├── service/            # Бизнес-логика
│   └── dao/                # Репозитории и сущности
│
├── booking/                 # Сервис бронирований
│   ├── controllers/        # REST контроллеры
│   ├── services/           # Бизнес-логика + Feign клиенты
│   ├── security/           # JWT фильтры и провайдеры
│   ├── repository/          # Репозитории
│   └── config/             # Retry конфигурация
│
├── hotel-service/          # Сервис управления отелями и номерами
│   ├── controllers/        # REST контроллеры
│   ├── service/            # Бизнес-логика
│   ├── security/           # JWT фильтры
│   └── repository/          # Репозитории
│
├── gateway/                # API Gateway
│   └── application.yaml    # Маршрутизация запросов
│
└── discovery/              # Eureka Service Discovery
```

## 🚀 Инструкция по запуску

### Требования

- Java 17 или выше
- Maven 3.6+
- 5+ ГБ свободной памяти

### Порядок запуска сервисов

**Важно:** Сервисы должны запускаться в следующем порядке:

1. **Eureka Discovery Server**
```bash
cd discovery
./mvnw spring-boot:run
# Сервис будет доступен на http://localhost:8761
```

2. **Auth Service**
```bash
cd auth
./mvnw spring-boot:run
# Сервис будет доступен на http://localhost:8084
```

3. **Hotel Service**
```bash
cd hotel-service
./mvnw spring-boot:run
# Сервис будет доступен на http://localhost:8085
```

4. **Booking Service**
```bash
cd booking
./mvnw spring-boot:run
# Сервис будет доступен на http://localhost:8083
```

5. **Gateway Service**
```bash
cd gateway
./mvnw spring-boot:run
# Gateway будет доступен на http://localhost:8080
```

### Проверка запуска

1. **Eureka Dashboard**: http://localhost:8761
   - Проверьте регистрацию всех сервисов

2. **Health Checks**:
   - Gateway: http://localhost:8080/actuator/health
   - Auth: http://localhost:8084/actuator/health
   - Booking: http://localhost:8083/actuator/health
   - Hotel: http://localhost:8085/actuator/health

### Запуск через IDE

1. Запустите все классы `*Application` в указанном выше порядке
2. Убедитесь, что все сервисы зарегистрированы в Eureka

### Переменные окружения

По умолчанию все сервисы используют:
- **Eureka**: `http://localhost:8761/eureka/`
- **H2 Database**: in-memory (данные не сохраняются после перезапуска)

## 📡 API Endpoints

### Через Gateway (http://localhost:8080)

#### Auth Service
- `POST /api/v1/auth/signUp` - Регистрация пользователя
- `GET /api/v1/access/customer/{email}` - Получение CustomerDetails по email
- `PUT /api/v1/access` - Обновление доступа
- `POST /api/v1/access` - Создание доступа
- `DELETE /api/v1/access/{email}` - Удаление доступа

#### Booking Service
- `GET /api/v1/bookings` - Список бронирований с фильтрами
- `GET /api/v1/bookings/{id}` - Получение бронирования по ID
- `POST /api/v1/bookings` - Создание бронирования
- `DELETE /api/v1/bookings/{id}` - Отмена бронирования
- `GET /api/v1/user/login/{login}` - Получение пользователя по login
- `POST /api/v1/user` - Создание пользователя
- `DELETE /api/v1/user/{id}` - Удаление пользователя

#### Hotel Service
- `GET /api/rooms` - Список доступных номеров
- `GET /api/rooms/recommend` - Рекомендуемые номера (сортировка по timesBooked)
- `POST /api/rooms` - Создание номера (требует ADMIN)
- `GET /api/hotels` - Список отелей
- `POST /api/hotels` - Создание отеля (требует ADMIN)

### Примеры запросов

#### Регистрация пользователя
```bash
curl -X POST http://localhost:8080/api/v1/auth/signUp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "login": "user",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### Создание бронирования (требует JWT токен)
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "userId": 1,
    "roomId": 1,
    "startDate": "2024-02-01",
    "endDate": "2024-02-05",
    "autoSelect": false
  }'
```

#### Автоматический выбор номера
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "userId": 1,
    "startDate": "2024-02-01",
    "endDate": "2024-02-05",
    "autoSelect": true
  }'
```

## 📝 ADR (Architecture Decision Records)

**Подробные ADR документы находятся в директории [`docs/adr/`](docs/adr/)**  
📚 [Индекс всех ADR документов](docs/adr/README.md)

### Краткое описание решений

### ADR-001: Микросервисная архитектура

**Статус:** ✅ Принято

**Контекст:** 
Требовалось создать масштабируемую систему управления бронированиями с возможностью независимого развертывания компонентов.

**Решение:**
Использована микросервисная архитектура с разделением на:
- `auth-service` - управление аутентификацией и авторизацией
- `booking-service` - управление бронированиями и пользователями
- `hotel-service` - управление отелями и номерами
- `gateway` - единая точка входа для всех запросов
- `discovery` - сервис обнаружения (Eureka)

**Последствия:**
- ✅ Независимое масштабирование сервисов
- ✅ Изоляция сбоев
- ✅ Разделение ответственности
- ⚠️ Усложнение коммуникации между сервисами
- ⚠️ Необходимость обеспечения согласованности данных

📄 [Подробный ADR-001](docs/adr/001-microservices-architecture.md)

---

### ADR-002: Saga Pattern для распределенных транзакций

**Статус:** ✅ Принято

**Контекст:**
Бронирование номера требует атомарной операции между `booking-service` и `hotel-service`, но классические ACID транзакции невозможны в распределенной системе.

**Решение:**
Реализован паттерн **Saga с компенсацией** (Compensating Transaction Pattern):

1. **Try Phase:**
   - Создание бронирования в статусе `PENDING`
   - Вызов `hotel-service.incrementRoomUsage()`

2. **Confirm Phase:**
   - При успехе: переход в статус `CONFIRMED`

3. **Compensate Phase:**
   - При ошибке: вызов `hotel-service.decrementRoomUsage()` для отката
   - Перевод бронирования в статус `CANCELLED`

**Реализация:**
```java
// BookingService.bookRoom()
try {
    entity = createBooking(PENDING);
    hotelService.incrementRoomUsage(rqId, roomId);
    entity.setStatus(CONFIRMED);
} catch (Exception e) {
    performCompensation(...); // вызывает decrementRoomUsage()
}
```

**Последствия:**
- ✅ Согласованность данных между сервисами
- ✅ Откат изменений при ошибках
- ⚠️ Возможна несогласованность при сбое компенсации
- ✅ Идемпотентность через `ProcessedRequest`

📄 [Подробный ADR-002](docs/adr/002-saga-pattern-compensation.md)

---

### ADR-003: Идемпотентность операций через ProcessedRequest

**Статус:** ✅ Принято

**Контекст:**
При сетевых сбоях или таймаутах запросы могут быть повторены. Необходимо обеспечить идемпотентность операций инкремента/декремента счетчика бронирований.

**Решение:**
Введена таблица `processed_requests` для отслеживания обработанных запросов:
- Первичный ключ: `request_id + operation_type`
- Проверка перед выполнением операции
- Сохранение записи после успешного выполнения

**Реализация:**
```java
if (processedRequestRepository.existsByRequestIdAndOperationType(requestId, "INCREMENT")) {
    return; // Идемпотентность - запрос уже обработан
}
// Выполнение операции...
processedRequestRepository.save(new ProcessedRequest(requestId, roomId, "INCREMENT"));
```

**Последствия:**
- ✅ Идемпотентность гарантирована
- ✅ Безопасные повторные попытки
- ⚠️ Необходимость очистки старых записей (не реализовано)

📄 [Подробный ADR-003](docs/adr/003-idempotency-processed-requests.md)

---

### ADR-004: Оптимистичная блокировка (Optimistic Locking)

**Статус:** ✅ Принято

**Контекст:**
Параллельные запросы на бронирование могут приводить к конфликтам при обновлении счетчика `timesBooked` в `hotel-service`.

**Решение:**
Использована **оптимистичная блокировка** с полем `version`:
- JPA аннотация `@Version` на сущностях `Room` и `BookingEntity`
- Автоматическая проверка версии при сохранении
- Retry механизм при `OptimisticLockingFailureException`:
  - Максимум 3 попытки
  - Экспоненциальная задержка: 100ms → 200ms → 400ms

**Реализация:**
```java
@Retryable(
    retryFor = OptimisticLockingFailureException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 100, multiplier = 2)
)
public void incrementTimesBooked(Long requestId, Long roomId) {
    // ...
}
```

**Последствия:**
- ✅ Высокая производительность (без блокировок БД)
- ✅ Поддержка параллельных запросов
- ⚠️ Требуется retry логика
- ✅ Автоматическая обработка конфликтов

📄 [Подробный ADR-004](docs/adr/004-optimistic-locking.md)

---

### ADR-005: Равномерное распределение нагрузки по номерам

**Статус:** ✅ Принято

**Контекст:**
При автоматическом выборе номера необходимо распределять нагрузку равномерно, чтобы избежать "простаивания" номеров.

**Решение:**
Использована сортировка номеров по `timesBooked ASC` при выборе:
- Запрос к БД: `findAvailableRoomsOrderedByTimesBooked()`
- Выбор первого доступного номера с наименьшим `timesBooked`
- Обеспечивает равномерное распределение нагрузки

**Реализация:**
```java
List<Room> availableRooms = roomRepository.findAvailableRoomsOrderedByTimesBooked();
Room selectedRoom = availableRooms.stream()
    .filter(room -> !excludeRooms.contains(room.getId()))
    .findFirst()
    .orElseThrow(...);
```

**Последствия:**
- ✅ Равномерное распределение нагрузки
- ✅ Минимизация простоя номеров
- ✅ Простота реализации
- ⚠️ Требует индекса на `times_booked` для производительности (рекомендуется)

📄 [Подробный ADR-005](docs/adr/005-load-balancing-rooms.md)

---

### ADR-006: Централизованное получение CustomerDetails через auth-service

**Статус:** ✅ Принято

**Контекст:**
`booking-service` и `hotel-service` нуждаются в информации о пользователе (userId, authorities) для авторизации, но не должны хранить пароли или иметь прямой доступ к БД auth-service.

**Решение:**
Создан централизованный endpoint в `auth-service`:
- `GET /api/v1/access/customer/{email}` возвращает `CustomerDetailsDto`
- `booking-service` и `hotel-service` используют Feign клиенты для получения данных
- Данные кешируются в SecurityContext после первой загрузки

**Реализация:**
```java
// booking-service
@FeignClient(value = "auth-service", path = "/api/v1/access")
public interface AccessService {
    @GetMapping("/customer/{email}")
    CustomerDetailsDto getCustomerDetails(@PathVariable String email);
}
```

**Последствия:**
- ✅ Единый источник правды для пользовательских данных
- ✅ Отсутствие дублирования паролей
- ✅ Централизованное управление ролями
- ⚠️ Дополнительный сетевой вызов при аутентификации
- ⚠️ Зависимость от доступности auth-service

📄 [Подробный ADR-006](docs/adr/006-centralized-customer-details.md)

---

### ADR-007: Retry механизм для устойчивости межсервисных вызовов

**Статус:** ✅ Принято

**Контекст:**
Сетевые вызовы между сервисами могут временно завершаться ошибками из-за сетевых проблем или временной недоступности сервисов.

**Решение:**
Настроен retry механизм в Feign клиентах:
- **Retry**: 3 попытки с задержкой от 1000ms до 3000ms
- **Timeout**: 5 секунд на подключение, 10 секунд на чтение
- Применяется к вызовам `hotel-service` из `booking-service`

**Конфигурация:**
```java
@Bean
public Retryer hotelRetryer() {
    return new Retryer.Default(1000, 3000, 3);
}

@Bean
public Request.Options requestOptions() {
    return new Request.Options(5000, 10000);
}
```

**Последствия:**
- ✅ Повышенная отказоустойчивость
- ✅ Автоматическое восстановление после временных сбоев
- ⚠️ Увеличение времени ответа при сбоях
- ✅ Настраиваемые параметры

---

### ADR-008: Spring Cloud Gateway для единой точки входа

**Статус:** ✅ Принято

**Контекст:**
Клиентам необходимо обращаться к нескольким микросервисам, что усложняет интеграцию и требует управления CORS и маршрутизацией.

**Решение:**
Внедрен **Spring Cloud Gateway** как единая точка входа:
- Все запросы идут через gateway (порт 8080)
- Централизованная маршрутизация по пути `/api/v1/**`
- Единая CORS конфигурация
- Load balancing через Eureka

**Маршрутизация:**
```yaml
routes:
  - id: booking-service-bookings
    uri: lb://booking-service
    predicates:
      - Path=/api/v1/bookings/**
```

**Последствия:**
- ✅ Упрощенная интеграция для клиентов
- ✅ Централизованное управление CORS
- ✅ Единая точка конфигурации
- ✅ Load balancing из коробки

---

## 🎯 Особенности реализации

### 1. Алгоритм планирования занятости номеров

- **Равномерное распределение**: Номера выбираются по приоритету `timesBooked ASC`
- **Параллельные бронирования**: Обрабатываются через оптимистичную блокировку с retry
- **Идемпотентность**: Гарантируется через `ProcessedRequest` для всех операций

### 2. Согласованность данных между сервисами

- **Saga с компенсацией**: Полная реализация отката при ошибках
- **Retry/Timeout**: Настроены для всех межсервисных вызовов
- **Идемпотентность**: Все операции с побочными эффектами идемпотентны

### 3. Безопасность

- **JWT токены**: Используются для аутентификации
- **Централизованная авторизация**: CustomerDetails получаются из auth-service
- **Role-based access**: Разделение прав ADMIN/USER

## 🧪 Тестирование

### Запуск тестов

```bash
# Все тесты
mvn test

# Тесты конкретного сервиса
cd auth && mvn test
cd booking && mvn test
cd hotel-service && mvn test
```

### Покрытие тестами

- ✅ Unit тесты для бизнес-логики
- ✅ Integration тесты для взаимодействия сервисов
- ✅ Тесты идемпотентности
- ✅ Тесты параллельных операций
- ✅ Тесты компенсации при ошибках

## 📊 Мониторинг

### Actuator Endpoints

Все сервисы предоставляют Actuator endpoints:
- `/actuator/health` - статус здоровья сервиса
- `/actuator/info` - информация о сервисе

### Логирование

Настроено структурированное логирование с использованием SLF4J:
- Все операции бронирования логируются с `rqId`
- Компенсационные операции логируются отдельно
- Ошибки содержат полный контекст

## 🔧 Конфигурация

### Основные настройки

Все конфигурации находятся в `src/main/resources/application.yaml` каждого сервиса:

- **Eureka**: `http://localhost:8761/eureka/`
- **H2 Database**: in-memory (для разработки)
- **JWT Secret**: настраивается в каждом сервисе
- **Retry**: 3 попытки с экспоненциальной задержкой

