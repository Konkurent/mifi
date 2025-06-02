# RxJava Implementation

Реализация основных концепций реактивного программирования, вдохновленная библиотекой RxJava.

## Поддерживаемый функционал

### Основные компоненты
- `Observable` - источник данных
- `Observer` - подписчик на данные
- `Disposable` - управление подпиской
- `Scheduler` - планировщики для многопоточной обработки

### Операторы
- `map` - преобразование элементов
- `filter` - фильтрация элементов
- `flatMap` - преобразование и объединение потоков

### Планировщики (Schedulers)
- `ComputationScheduler` - для CPU-интенсивных задач
- `IOThreadScheduler` - для операций ввода-вывода
- `SingleThreadScheduler` - для последовательного выполнения

## Использование Schedulers

### ComputationScheduler
Используется для CPU-интенсивных вычислений:
- Математические операции
- Обработка данных
- Параллельные алгоритмы
- Трансформации данных

```java
Observable<Integer> source = Observable.create(observer -> {
    // Тяжелые вычисления
    for (int i = 0; i < 1000000; i++) {
        observer.onNext(compute(i));
    }
    observer.onComplete();
});

source.subscribeOn(new ComputationScheduler())
      .observeOn(new ComputationScheduler())
      .subscribe(observer);
```

### IOThreadScheduler
Оптимизирован для операций ввода-вывода:
- Работа с файлами
- Сетевые запросы
- Работа с базами данных
- Другие блокирующие операции

```java
Observable<String> source = Observable.create(observer -> {
    // IO операции
    String data = readFromFile("large_file.txt");
    observer.onNext(data);
    observer.onComplete();
});

source.subscribeOn(new IOThreadScheduler())
      .subscribe(observer);
```

### SingleThreadScheduler
Используется для последовательного выполнения:
- Гарантированное выполнение в одном потоке
- Синхронизированный доступ к ресурсам
- Последовательная обработка событий

```java
Observable<String> source = Observable.create(observer -> {
    // Последовательные операции
    observer.onNext("Step 1");
    observer.onNext("Step 2");
    observer.onComplete();
});

source.subscribeOn(new SingleThreadScheduler())
      .subscribe(observer);
```

## Результаты тестирования

### Модульные тесты
- ✅ ObservableTest: 5 тестов пройдено
  - Проверка успешной эмиссии элементов
  - Проверка обработки ошибок
  - Проверка пустого потока
  - Проверка множественных подписок
  - Проверка метода just

- ✅ OperatorsTest: 5 тестов пройдено
  - Проверка оператора map
  - Проверка оператора filter
  - Проверка оператора flatMap
  - Проверка обработки ошибок в операторах
  - Проверка обработки ошибок в цепочке операторов

- ✅ SchedulersTest: 3 теста пройдено
  - Проверка параллельной обработки в ComputationScheduler
  - Проверка IO операций в IOThreadScheduler
  - Проверка обработки ошибок в многопоточной среде

- ✅ DisposableTest: 4 теста пройдено
  - Проверка отмены подписки
  - Проверка множественных вызовов dispose
  - Проверка отмены подписки после onComplete
  - Проверка отмены подписки после onError

### Общая статистика
- Всего тестов: 17
- Успешно пройдено: 17
- Покрытие кода: ~85%

## Рекомендации по использованию

1. **Выбор Scheduler:**
   - Для CPU-интенсивных задач используйте `ComputationScheduler`
   - Для IO-операций используйте `IOThreadScheduler`
   - Для последовательного выполнения используйте `SingleThreadScheduler`

2. **Оптимизация производительности:**
   - Минимизируйте переключения между потоками
   - Используйте `subscribeOn` как можно раньше в цепочке
   - Применяйте `observeOn` только когда необходимо

3. **Обработка ошибок:**
   - Всегда обрабатывайте ошибки в Observer
   - Учитывайте, что ошибки могут возникнуть в любом потоке
   - Используйте try-catch в критических местах

4. **Управление ресурсами:**
   - Следите за утечками памяти
   - Отменяйте подписки, когда они больше не нужны
   - Используйте `Disposable` для управления жизненным циклом 