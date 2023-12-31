### Задача: Создание собственного Gradle плагина для анализа кода

#### Цель

Разработать Gradle плагин, который анализирует исходный код в проекте и генерирует отчёт о количестве классов, методов и
строк кода. Плагин должен быть написан на Kotlin и использовать Gradle Kotlin DSL.

#### Требования

1. Плагин должен сканировать все `.java` и `.kt` файлы в директории `src`.
2. Подсчитать количество классов, методов и строк кода.
3. Генерировать отчёт в формате JSON.

#### Пример структуры JSON отчёта

```json
{
  "totalClasses": 10,
  "totalMethods": 40,
  "totalLines": 1000
}
```

#### Шаги для выполнения

1. Создайте новый Kotlin проект с использованием Gradle.
2. Изучите базовую структуру плагина в Gradle.
3. Реализуйте логику подсчёта классов, методов и строк кода.
   Подсказка: Посмотреть на тулзу ANTLR - implementation("org.antlr:antlr4:4.13.1")
   ANTLR (ANother Tool for Language Recognition) — это мощный инструмент для создания парсеров. Он может анализировать
   Java, Kotlin и множество других языков.
4. Добавьте возможность генерации отчёта в формате JSON.
5. Протестируйте ваш плагин на тестовом проекте.

#### Пример отработанной таски gradle generateProjectStatistic:

```json
{
  "totalClasses": 10,
  "totalMethods": 40,
  "totalLines": 1000
}
```