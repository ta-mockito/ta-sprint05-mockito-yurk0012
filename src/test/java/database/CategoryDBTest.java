package database;

import dao.CategoryDB;
import model.Category;
import model.Child;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import utils.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Category Database Operations Tests")
class CategoryDBTest {

    private static final String EXISTING_TITLE = "Old Category";
    private static final String SPORTS_CATEGORY_TITLE = "Sports uniquE Activities";
    private static final String SQL_SELECT_CATEGORY_ID_BY_TITLE = "SELECT id FROM categories WHERE title = ?";
    private static final String SQL_SELECT_CATEGORY_TITLE_BY_ID = "SELECT title FROM categories WHERE id = ?";


    private CategoryDB db;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        new DBUtil().executeFile("init.sql");
        connection = DBUtil.getConnection();
        db = new CategoryDB(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (db != null) {
            db.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @ParameterizedTest(name = "addCategory title=''{0}'' changes count by {1}")
    @CsvSource({
            "Old Category,0",
            "Educational Workshop,1"
    })
    @DisplayName("Should handle addCategory according to unique title constraint")
    void addCategoryShouldRespectUniqueTitleConstraint(String rawTitle, int expectedCountDelta) throws SQLException {
        int oldCount = DBUtil.totalCount("categories");
        String title = expectedCountDelta == 1 ? rawTitle + " " + System.nanoTime() : rawTitle;
        String avatarUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII";

        Category result = db.addCategory(new Category(avatarUrl, title));

        assertEquals(oldCount + expectedCountDelta, DBUtil.totalCount("categories"));
        assertNotNull(result.id(), "Result should have an ID");
        assertEquals(title, result.title(), "Result should preserve category title");
    }

    @ParameterizedTest
    @MethodSource("invalidAddCategories")
    @DisplayName("Should throw IllegalArgumentException for invalid category in addCategory")
    void addCategoryShouldThrowExceptionForInvalidInput(Category invalidCategory, String expectedMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> db.addCategory(invalidCategory));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should fail DB constraint when addCategory has null avatar")
    void addCategoryShouldThrowSQLExceptionForNullAvatar(String avatar) {
        assertThrows(SQLException.class, () -> db.addCategory(new Category(avatar, "Avatar Required " + System.nanoTime())));
    }

    @ParameterizedTest(name = "updateCategory for id {0} should return {1}")
    @MethodSource("updateArguments")
    @DisplayName("Should update existing category and return false for non-existing ID")
    void updateCategoryShouldHandleExistingAndMissingId(Long id, boolean expectedResult) throws SQLException {
        String updatedTitle = "Updated category " + System.nanoTime();
        String updatedAvatar = "data:image/png;base64,UPDATED";

        boolean updateResult = db.updateCategory(new Category(id, updatedAvatar, updatedTitle));

        assertEquals(expectedResult, updateResult);
        if (expectedResult) {
            assertEquals(updatedTitle, findCategoryTitleById(id));
        }
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateCategories")
    @DisplayName("Should throw IllegalArgumentException for invalid category in updateCategory")
    void updateCategoryShouldThrowExceptionForInvalidInput(Category invalidCategory, String expectedMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> db.updateCategory(invalidCategory));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("duplicateUpdateArguments")
    @DisplayName("Should fail unique title constraint when updating to duplicate title")
    void updateCategoryShouldThrowSQLExceptionForDuplicateTitle(Long categoryId, String duplicateTitle) {
        assertThrows(SQLException.class,
                () -> db.updateCategory(new Category(categoryId, "data:image/png;base64,UPDATED", duplicateTitle)));
    }

    @ParameterizedTest(name = "deleteCategory id={0} should return {1}")
    @MethodSource("deleteArguments")
    @DisplayName("Should delete existing category and return false for non-existing ID")
    void deleteCategoryShouldHandleExistingAndMissingId(Long id, boolean expectedResult) throws SQLException {
        int oldCount = DBUtil.totalCount("categories");

        boolean deleteResult = db.deleteCategory(id);

        assertEquals(expectedResult, deleteResult);
        assertEquals(oldCount + (expectedResult ? -1 : 0), DBUtil.totalCount("categories"));
    }

    @ParameterizedTest
    @MethodSource("referencedCategoryIds")
    @DisplayName("Should fail deleting category referenced by club (FK restriction)")
    void deleteCategoryShouldThrowSQLExceptionWhenCategoryIsReferenced(Long categoryId) {
        assertThrows(SQLException.class, () -> db.deleteCategory(categoryId));
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should throw IllegalArgumentException when deleting category with null ID")
    void deleteCategoryShouldThrowExceptionForNullId(Long id) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> db.deleteCategory(id));
        assertEquals("Category ID cannot be null", exception.getMessage());
    }

    @ParameterizedTest(name = "titlePart=''{0}'' should return {1} categories")
    @CsvSource({
            "uniquE,4",
            "UNIQUE,4",
            "Category,5",
            "Arts,1",
            "Missing value,0",
            "'',9"
    })
    @DisplayName("Should find categories by title part according to seeded SQL data")
    void findCategoriesByTitlePartShouldReturnExpectedCount(String titlePart, int expectedCount) throws SQLException {
        String resolvedTitlePart = "''".equals(titlePart) ? "" : titlePart;
        List<Category> categories = db.findCategoriesByTitlePart(resolvedTitlePart);
        assertEquals(expectedCount, categories.size());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should throw IllegalArgumentException when finding categories with null title part")
    void findCategoriesByTitlePartShouldThrowExceptionForNullTitlePart(String titlePart) {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> db.findCategoriesByTitlePart(titlePart));
        assertEquals("Title part cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should throw IllegalArgumentException when finding children with null category ID")
    void findAllChildrenInCategoryShouldThrowExceptionForNullCategoryId(Long categoryId) {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> db.findAllChildrenInCategory(categoryId));
        assertEquals("Category ID cannot be null", exception.getMessage());
    }

    @ParameterizedTest(name = "category=''{0}'' should have {1} children")
    @MethodSource("childrenByCategoryArguments")
    @DisplayName("Should return children mapped to category according to seeded club/child relations")
    void findAllChildrenInCategoryShouldReturnExpectedChildren(String categoryTitle,
                                                               int expectedChildrenCount,
                                                               String expectedFirstNamesCsv) throws SQLException {
        Long categoryId = findCategoryIdByTitle(categoryTitle);
        assertNotNull(categoryId, "Category should exist in seeded data: " + categoryTitle);

        List<Child> children = db.findAllChildrenInCategory(categoryId);
        assertEquals(expectedChildrenCount, children.size());

        if (!expectedFirstNamesCsv.isBlank()) {
            Set<String> actualFirstNames = children.stream().map(Child::firstName).collect(Collectors.toSet());
            Set<String> expectedFirstNames = Arrays.stream(expectedFirstNamesCsv.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            assertTrue(actualFirstNames.containsAll(expectedFirstNames));
        }
    }

    @ParameterizedTest
    @CsvSource({"Science,Charlotte"})
    @DisplayName("Should map child with null birth date when reading children in category")
    void findAllChildrenInCategoryShouldMapNullBirthDate(String categoryTitle, String expectedFirstName) throws SQLException {
        Long categoryId = findCategoryIdByTitle(categoryTitle);
        assertNotNull(categoryId);

        Long clubId = insertClub(categoryId, "Null birth date club " + System.nanoTime());
        linkChildToClub(clubId, 10L);

        List<Child> children = db.findAllChildrenInCategory(categoryId);

        Child child = children.stream()
                .filter(c -> expectedFirstName.equals(c.firstName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected child not found: " + expectedFirstName));
        assertNull(child.birthDate(), "Birth date should be null for Charlotte from seeded data");
    }

    private Long insertClub(Long categoryId, String title) throws SQLException {
        String sql = "INSERT INTO club (title, description, image_url, category_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, title);
            pst.setString(2, "club for null birth date branch coverage");
            pst.setString(3, null);
            pst.setLong(4, categoryId);
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to create club for test");
    }

    private void linkChildToClub(Long clubId, Long childId) throws SQLException {
        String sql = "INSERT INTO club_child (club_id, child_id) VALUES (?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setLong(1, clubId);
            pst.setLong(2, childId);
            pst.executeUpdate();
        }
    }

    private Long findCategoryIdByTitle(String title) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQL_SELECT_CATEGORY_ID_BY_TITLE)) {
            pst.setString(1, title);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        }
    }

    private String findCategoryTitleById(Long categoryId) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(SQL_SELECT_CATEGORY_TITLE_BY_ID)) {
            pst.setLong(1, categoryId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getString("title") : null;
            }
        }
    }

    private static Stream<Arguments> invalidAddCategories() {
        return Stream.of(
                Arguments.of(null, "Category cannot be null"),
                Arguments.of(new Category("avatar-url", null), "Category title cannot be null or empty"),
                Arguments.of(new Category("avatar-url", ""), "Category title cannot be null or empty"),
                Arguments.of(new Category("avatar-url", "   "), "Category title cannot be null or empty")
        );
    }

    private static Stream<Arguments> invalidUpdateCategories() {
        return Stream.of(
                Arguments.of(null, "Category cannot be null"),
                Arguments.of(new Category(null, "avatar-url", "Valid Title"), "Category ID cannot be null"),
                Arguments.of(new Category(1L, "avatar-url", null), "Category title cannot be null or empty"),
                Arguments.of(new Category(1L, "avatar-url", ""), "Category title cannot be null or empty"),
                Arguments.of(new Category(1L, "avatar-url", "   "), "Category title cannot be null or empty")
        );
    }

    private static Stream<Arguments> childrenByCategoryArguments() {
        return Stream.of(
                Arguments.of("Old Category", 0, ""),
                Arguments.of(SPORTS_CATEGORY_TITLE, 4, "John,Michael,William,Olivia"),
                Arguments.of("Arts & Crafts", 3, "Emma,Sophia,Ava"),
                Arguments.of("Music", 2, "Emma,James"),
                Arguments.of("Science", 2, "John,Alexander")
        );
    }

    private static Stream<Arguments> duplicateUpdateArguments() {
        return Stream.of(
                Arguments.of(2L, EXISTING_TITLE),
                Arguments.of(7L, "Music")
        );
    }

    private static Stream<Arguments> updateArguments() {
        return Stream.of(
                Arguments.of(2L, true),
                Arguments.of(1000L, false)
        );
    }

    private static Stream<Arguments> deleteArguments() {
        return Stream.of(
                Arguments.of(2L, true),
                Arguments.of(1000L, false)
        );
    }

    private static Stream<Long> referencedCategoryIds() {
        return Stream.of(6L, 7L, 8L, 9L);
    }
}
