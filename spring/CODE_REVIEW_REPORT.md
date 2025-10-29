# Отчет о проверке системы бронирования

## Дата проверки: 2024

## Критерии проверки

### 1. Реализация алгоритма планирования занятости номеров

#### ✅ Обработка параллельных бронирований и идемпотентность

**Статус: РЕАЛИЗОВАНО**

**Реализовано:**
- Идемпотентность через `ProcessedRequest` в `RoomService.incrementTimesBooked()` (строки 75-78)
- Использование `@Version` для оптимистичной блокировки в `Room` и `BookingEntity`
- Обработка `OptimisticLockingFailureException` с retry механизмом:
  - `@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))` в `RoomService`
- Проверка на дубликаты бронирований в `assertRoomFreedom()` (строки 133-148 в `BookingService`)

**Тесты:**
- `RoomServiceIntegrationTest.testConcurrentIncrement_OptimisticLocking()` - проверяет параллельные инкременты
- `BookingServiceIntegrationTest.testBookRoom_ConcurrentBooking_PreventsDuplicate()` - проверяет предотвращение дубликатов

#### ⚠️ Корректная работа под нагрузкой

**Статус: ЧАСТИЧНО РЕАЛИЗОВАНО**

**Реализовано:**
- Оптимистичная блокировка предотвращает конфликты
- Retry механизм для обработки временных сбоев

**Проблемы:**
- Нет явных нагрузочных тестов (stress tests)
- Обработка `OptimisticLockingFailureException` в `BookingService` просто выбрасывает исключение без повторной попытки (строки 107-111)
- При большом количестве параллельных запросов может быть много повторных попыток

#### ❌ Равномерное распределение номеров без «простаивания»

**Статус: НЕ РЕАЛИЗОВАНО**

**Проблема:**
Метод `resolveAvailableRoomId()` в `RoomService` (строки 105-116) использует:
```java
List<Room> availableRooms = roomRepository.findAllAvailableRooms();
Room selectedRoom = availableRooms.stream()
    .filter(room -> !excludeRooms.contains(room.getId()))
    .findFirst()  // ❌ Берет первый попавшийся номер без сортировки
```

**Ожидаемое поведение:**
В репозитории есть метод `findAvailableRoomsOrderedByTimesBooked()` (строка 18 в `RoomRepository`), который сортирует по `timesBooked ASC`, но он НЕ используется в `resolveAvailableRoomId()`.

**Следствие:**
- Номера выбираются в произвольном порядке
- Нет гарантии равномерного распределения нагрузки
- Номера с меньшим `timesBooked` не имеют приоритета

**Рекомендация:**
Использовать `findAvailableRoomsOrderedByTimesBooked()` вместо `findAllAvailableRooms()` для обеспечения равномерного распределения.

---

### 2. Реализация согласованности данных между сервисами

#### ✅ Устойчивость вызовов (Retry/Timeout на взаимодействии с Hotel)

**Статус: РЕАЛИЗОВАНО**

**Реализовано:**
- Retry конфигурация в `HotelRetryerConfig`:
  - `Retryer.Default(1000, 3000, 3)` - 3 попытки, задержка от 1000ms до 3000ms
- Timeout конфигурация:
  - `Request.Options(5000, 10000)` - 5s на подключение, 10s на чтение
- Применяется через `@FeignClient(configuration = HotelRetryerConfig.class)` в `HotelService`

#### ⚠️ Сага с компенсацией

**Статус: ЧАСТИЧНО РЕАЛИЗОВАНО**

**Реализовано:**
- При ошибке бронирования выполняется компенсация: статус изменяется на `CANCELLED` (строки 117-128 в `BookingService`)
- Используется паттерн "Try-Confirm" с частичной компенсацией

**КРИТИЧЕСКИЕ ПРОБЛЕМЫ:**

1. **Отсутствие отката счетчика при ошибке:**
   - Если `incrementRoomUsage()` выполнен успешно, но затем произошла ошибка в `BookingService`, счетчик в `hotel-service` НЕ уменьшается
   - В коде (строки 94-103 в `BookingService`) после успешного `incrementRoomUsage()` бронирование переходит в `CONFIRMED`, но если ошибка происходит после этого, счетчик остается увеличенным
   - **Проблема:** Нет вызова `decrementRoomUsage()` в компенсационном блоке

2. **Отсутствие decrement при отмене бронирования:**
   - Метод `cancelBooking()` (строки 150-157) только меняет статус на `CANCELLED`, но НЕ вызывает уменьшение счетчика в `hotel-service`
   - **Следствие:** При отмене подтвержденного бронирования счетчик `timesBooked` остается некорректным

3. **Нет полноценного механизма Saga:**
   - Нет координатора саги
   - Нет явных состояний саги (PENDING, CONFIRMED, COMPENSATING, COMPENSATED)
   - Компенсация выполняется только локально в `BookingService`, без отката изменений в `hotel-service`

**Рекомендации:**
1. Добавить метод `decrementRoomUsage()` в `HotelService` и `RoomService`
2. Вызывать `decrementRoomUsage()` в компенсационном блоке при ошибке после `incrementRoomUsage()`
3. Вызывать `decrementRoomUsage()` при отмене бронирования со статусом `CONFIRMED`
4. Рассмотреть использование полноценного Saga pattern (например, через Spring Cloud Saga или Event-driven подход)

---

## Сводка проблем

### Критические:
1. ❌ **Нет равномерного распределения номеров** - используется `findFirst()` без сортировки
2. ❌ **Нет отката счетчика при ошибке** - после `incrementRoomUsage()` нет `decrementRoomUsage()` при компенсации
3. ❌ **Нет decrement при отмене бронирования** - `cancelBooking()` не уменьшает счетчик

### Средние:
4. ⚠️ **Нет повторной попытки при `OptimisticLockingFailureException` в `BookingService`** - просто выбрасывается исключение
5. ⚠️ **Нет явных нагрузочных тестов** - только unit и integration тесты

### Положительные моменты:
- ✅ Идемпотентность реализована корректно
- ✅ Retry/Timeout настроены правильно
- ✅ Оптимистичная блокировка работает
- ✅ Есть базовые тесты для параллельных операций

---

## Рекомендации по исправлению

1. **Исправить равномерное распределение:**
   - Заменить `findAllAvailableRooms()` на `findAvailableRoomsOrderedByTimesBooked()` в `resolveAvailableRoomId()`

2. **Добавить decrement механизм:**
   - Создать `decrementRoomUsage()` метод с идемпотентностью (через `ProcessedRequest` с типом "DECREMENT")
   - Вызывать его в компенсационном блоке после ошибки
   - Вызывать его в `cancelBooking()` для подтвержденных бронирований

3. **Улучшить обработку оптимистичной блокировки:**
   - Добавить retry механизм в `BookingService.bookRoom()` для `OptimisticLockingFailureException`

4. **Добавить нагрузочные тесты:**
   - Создать stress-тесты для проверки работы под высокой нагрузкой

