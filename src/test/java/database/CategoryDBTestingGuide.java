package database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import static org.junit.jupiter.api.Assertions.*;

/**
 * ДЕМОНСТРАЦІЯ КОНЦЕПЦІЇ ТЕСТУВАННЯ НА ПРИКЛАДІ CategoryDAO
 *
 * ПОКРИВАЄ ТРИ ПІДХОДИ:
 * 1. ІНТЕГРАЦІЙНІ ТЕСТИ (CategoryDBTest) - з реальною БД PostgreSQL
 * 2. МОКАМИ (CategoryDBMockTest) - з Mockito фреймворком
 * 3. ДОКУМЕНТАЦІЯ (TESTING_GUIDE.md) - пояснення всіх підходів
 *
 * ЦЕЙ ФАЙЛ ДЕМОНСТРУЄ РЕКОМЕНДАЦІЇ
 * ================================
 *
 * ✅ КОЛИ ТЕСТУВАТИ ІНТЕГРАЦІЙНО (CategoryDBTest.java):
 * - Повний DAO тестування з реальною БД
 * - Знайти баги у SQL запитах
 * - Перевірити обмеження БД (UNIQUE, FK constraints)
 * - Тестувати складні SQL операції
 *
 * ✅ КОЛИ ТЕСТУВАТИ З МОКАМИ (CategoryDBMockTest.java):
 * - Юнітест DAO підготовки запитів
 * - Мокування зовнішніх залежностей
 * - Тестування граничних випадків (null, exception)
 * - Верифікація послідовності викликів
 *
 * ✅ ДОКУМЕНТАЦІЯ (TESTING_GUIDE.md):
 * - Посилання на Mockito документацію
 * - Порівняння підходів
 * - Best practices
 *
 * ЗАПУСК ТЕСТІВ:
 * ===============
 *
 * # Усі тести CategoryDB
 * mvn test -k CategoryDB
 *
 * # Тільки інтеграційні тести (потребує БД)
 * mvn test -Dtest=CategoryDBTest
 *
 * # Тільки тести з Mockito
 * mvn test -Dtest=CategoryDBMockTest
 *
 * # З покриттям
 * mvn clean test jacoco:report
 */
class CategoryDBTestingGuide {

    /**
     * ЗАМІТКА 1: ПАРАМЕТРИЗОВАНІ ТЕСТИ
     *
     * @ParameterizedTest дозволяє запустити один тест з багатьма параметрами
     * Це знижує дублювання коду та покращує масштабованість
     */
    @ParameterizedTest
    @CsvSource({
            "Sports|avatar1|true",
            "Science|avatar2|true",
            "Arts|avatar3|true"
    })
    @DisplayName("Демо: @ParameterizedTest не потребує писати три окремих тести")
    void demonstrateParameterizedTest(String title, String avatar, boolean expected) {
        // Замість копіювання тесту 3 рази, використовуємо 1 метод!
        assertTrue(expected, "На прикладі параметризованих тестів");
    }

    /**
     * ЗАМІТКА 2: СТРУКТУРА ТЕСТУ - AAA Pattern
     *
     * ARRANGE (Підготовка) - налаштуємо дані та об'єкти
     * ACT (Виконання)     - виконуємо метод
     * ASSERT (Перевірка)  - перевіряємо результат
     *
     * ✅ ДОБРЕ:
     * void testAddCategory() {
     *     // ARRANGE
     *     Category input = new Category("avatar", "Sports");
     *     // ACT
     *     Category result = db.addCategory(input);
     *     // ASSERT
     *     assertEquals("Sports", result.title());
     * }
     *
     * ❌ ПОГАНО:
     * void testAddCategory() {
     *     Category input = new Category("avatar", "Sports");
     *     Category result = db.addCategory(input);
     *     println(result);  // Що це? Тест без assertion?
     * }
     */
    @ParameterizedTest
    @DisplayName("Демонстрація структури: ARRANGE → ACT → ASSERT")
    @CsvSource({"AAA Pattern"})
    void demonstrateAAAPattern(String concept) {
        // ARRANGE - підготовка
        String title = "Sports";

        // ACT - виконання
        String result = title.toUpperCase();

        // ASSERT - перевірка
        assertEquals("SPORTS", result,
            "Структура ARRANGE-ACT-ASSERT показує логіку тесту");
    }

    /**
     * ЗАМІТКА 3: MOCKITO vs ІНТЕГРАЦІЙНІ ТЕСТИ
     *
     * MOCKITO ➕:
     * ✓ Швидко виконується
     * ✓ Не потребує БД
     * ✓ Ізольовані од зовнішніх залежностей
     * ✓ Можна перевірити виклики методів
     *
     * MOCKITO ➖:
     * ✗ Не перевіряє реальні обмеження БД
     * ✗ "Зелені" тести можуть бути помилкова
     * ✗ Більше коду налаштування мок-ів
     *
     * ІНТЕГРАЦІЙНІ ➕:
     * ✓ Перевіряють реальну практику
     * ✓ Знаходять баги у SQL запитах
     * ✓ База UNIQUE, FK обмеження
     * ✓ Забезпечують надійність
     *
     * ІНТЕГРАЦІЙНІ ➖:
     * ✗ Повільніший
     * ✗ Потребує БД
     * ✗ Складно налаштувати CI/CD
     * ✗ Нестабільні через мережу
     */
    @ParameterizedTest
    @CsvSource({"Mockito дозволяє швидко писати unit-тести"})
    @DisplayName("Порівняння: Mockito vs Інтеграційне тестування")
    void demonstrateMockitoVsIntegration(String note) {
        // Це не реальний тест, тільки демонстрація
        assertNotNull(note, note);
    }


    @ParameterizedTest
    @CsvSource({
            "null_value",
            "empty_string",
            "blank_string"
    })
    @DisplayName("Демо: Тестування обробки помилок")
    void demonstrateErrorHandling(String scenario) {
        // У реальному тесту:
        // assertThrows(IllegalArgumentException.class, () -> {
        //     db.addCategory(null);
        // });

        assertTrue(true, "Завжди тестуйте помилкові випадки");
    }

    /**
     * ЗАМІТКА 5: FIXTURE - Повторне використання даних
     *
     * Вміст @BeforeEach виконується перед КОЖНИМ тестом
     * Це гарантує, що тести не впливають один на одного
     *
     * ✅ ДОБРЕ:
     * @BeforeEach
     * void setUp() {
     *     db = new CategoryDB(connection); // Нова інстанція для кожного тесту!
     * }
     *
     * ❌ ПОГАНО:
     * private CategoryDB db = new CategoryDB(...)  // Один об'єкт для всіх тестів!
     */
    @DisplayName("Демо: @BeforeEach створює свіжі об'єкти для кожного тесту")
    @ParameterizedTest
    @CsvSource({"1", "2", "3"})
    void demonstrateBeforeEach(String testNumber) {
        assertTrue(true, "Кожен тест має своє окремо setUp()");
    }

    /**
     * ПОСЛІДОВНІСТЬ ЗАПУСКУ ТЕСТІВ:
     *
     * CategoryDBTest (інтеграційні):
     *   1. setUp()
     *   2. testAddCategory()
     *   3. tearDown()
     *   4. setUp()
     *   5. testUpdateCategory()
     *   6. tearDown()
     *   ... і т.д.
     *
     * Кожен @BeforeEach / @AfterEach виконується окремо!
     */
}

