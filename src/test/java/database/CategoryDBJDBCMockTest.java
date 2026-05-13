package database;

import dao.CategoryDB;
import model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JDBC-level mock tests for CategoryDB")
class CategoryDBJDBCMockTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private ResultSet generatedKeys;

    private CategoryDB categoryDB;

    @BeforeEach
    void setUp() {
        categoryDB = new CategoryDB(connection);
    }

    @Test
    @DisplayName("addCategory: returns existing if found")
    void addCategoryShouldReturnExistingWhenFound() throws SQLException {
        Category input = new Category(null, "icon", "Existing");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("avatar")).thenReturn("icon");
        when(resultSet.getString("title")).thenReturn("Existing");

        Category result = categoryDB.addCategory(input);

        assertEquals(1L, result.id());
        assertEquals("Existing", result.title());
        verify(preparedStatement, never()).executeUpdate();
    }

    @Test
    @DisplayName("addCategory: inserts new if not found")
    void addCategoryShouldInsertWhenNotFound() throws SQLException {
        Category input = new Category(null, "new_icon", "New Cat");

        // First call: select to check existence
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Not found

        // Second call: insert
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Mocking getGeneratedKeys
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true); // Generated key exists
        when(generatedKeys.getLong(1)).thenReturn(77L);

        Category result = categoryDB.addCategory(input);

        assertEquals(77L, result.id());
        assertEquals("New Cat", result.title());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("addCategory: throws exception if generated keys missing")
    void addCategoryShouldThrowWhenKeysMissing() throws SQLException {
        Category input = new Category(null, "icon", "Fail");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(false); // No key!

        assertThrows(SQLException.class, () -> categoryDB.addCategory(input));
    }

    @Test
    @DisplayName("updateCategory: returns true if affected rows == 1")
    void updateCategoryShouldReturnTrue() throws SQLException {
        Category input = new Category(1L, "avatar", "Updated");
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean updated = categoryDB.updateCategory(input);

        assertTrue(updated);
    }

    @Test
    @DisplayName("updateCategory: returns false if affected rows == 0")
    void updateCategoryShouldReturnFalse() throws SQLException {
        Category input = new Category(1L, "avatar", "Updated");
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean updated = categoryDB.updateCategory(input);

        assertFalse(updated);
    }

    @Test
    @DisplayName("findCategoriesByTitlePart: returns matching categories")
    void findCategoriesByTitlePartShouldReturnList() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // 2 results
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("avatar")).thenReturn("icon1", "icon2");
        when(resultSet.getString("title")).thenReturn("Cat 1", "Cat 2");

        List<Category> results = categoryDB.findCategoriesByTitlePart("cat");

        assertEquals(2, results.size());
        assertEquals("Cat 1", results.getFirst().title());
        assertEquals("icon1", results.getFirst().avatar());
    }

    @Test
    @DisplayName("Validation: addCategory(null) throws exception")
    void addCategoryShouldValidateNull() {
        assertThrows(IllegalArgumentException.class, () -> categoryDB.addCategory(null));
    }
}
























