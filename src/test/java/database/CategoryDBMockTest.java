package database;

import dao.CategoryDAO;
import model.Category;
import model.Child;
import service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mock demo for category flow")
class CategoryDBMockTest {

    @Mock
    private CategoryDAO categoryDAO;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Mock: search delegates to DAO with normalized input")
    void searchCategoriesShouldNormalizeAndDelegate() throws SQLException {
        when(categoryDAO.findCategoriesByTitlePart("sport")).thenReturn(List.of(
                new Category(1L, "avatar", "Sports Activities")
        ));

        List<Category> result = categoryService.searchCategories("  sport  ");

        assertEquals(1, result.size());
        assertEquals("Sports Activities", result.get(0).title());
        verify(categoryDAO).findCategoriesByTitlePart("sport");
    }

    @Test
    @DisplayName("Mock: search throws IllegalArgumentException for null/blank input")
    void searchCategoriesShouldValidateInput() {
        assertThrows(IllegalArgumentException.class, () -> categoryService.searchCategories(null));
        assertThrows(IllegalArgumentException.class, () -> categoryService.searchCategories("   "));
    }

    @Test
    @DisplayName("Mock: search translates SQLException to DatabaseException")
    void searchCategoriesShouldTranslateException() throws SQLException {
        when(categoryDAO.findCategoriesByTitlePart(any())).thenThrow(new SQLException("DB error"));

        assertThrows(dao.exception.DatabaseException.class, () -> categoryService.searchCategories("test"));
    }

    @Test
    @DisplayName("Mock: rename delegates update with expected id and title")
    void renameCategoryShouldCallUpdate() throws SQLException {
        when(categoryDAO.updateCategory(any(Category.class))).thenReturn(true);

        boolean renamed = categoryService.renameCategory(7L, "New Title");

        assertEquals(true, renamed);
        verify(categoryDAO).updateCategory(new Category(7L, null, "New Title"));
    }

    @Test
    @DisplayName("Mock: rename validates null ID and blank title")
    void renameCategoryShouldValidateInput() throws SQLException {
        assertThrows(IllegalArgumentException.class,
                () -> categoryService.renameCategory(null, "Title"));
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoryService.renameCategory(1L, "   "));

        assertEquals("New title cannot be null or blank", ex.getMessage());
        verify(categoryDAO, never()).updateCategory(any(Category.class));
    }

    @Test
    @DisplayName("Mock: rename throws DatabaseException when update returns false")
    void renameCategoryShouldThrowWhenNotFound() throws SQLException {
        when(categoryDAO.updateCategory(any())).thenReturn(false);

        assertThrows(dao.exception.DatabaseException.class, 
                () -> categoryService.renameCategory(1L, "New Title"));
    }

    @Test
    @DisplayName("Mock: service can map children count via DAO")
    void countChildrenShouldUseDaoResult() throws SQLException {
        when(categoryDAO.findAllChildrenInCategory(5L)).thenReturn(List.of(
                new Child(1L, "John", "Doe", null),
                new Child(2L, "Emma", "Stone", null)
        ));

        int count = categoryService.countChildren(5L);

        assertEquals(2, count);
        verify(categoryDAO).findAllChildrenInCategory(5L);
    }

    @Test
    @DisplayName("Mock: countChildren handles null list from DAO")
    void countChildrenShouldHandleNullResult() throws SQLException {
        when(categoryDAO.findAllChildrenInCategory(5L)).thenReturn(null);

        assertEquals(0, categoryService.countChildren(5L));
    }

    @Test
    @DisplayName("Mock: addCategory happy path")
    void addCategoryShouldDelegateToDao() throws SQLException {
        Category input = new Category(null, "icon", "New Cat");
        Category expected = new Category(100L, "icon", "New Cat");
        when(categoryDAO.addCategory(input)).thenReturn(expected);

        Category result = categoryService.addCategory(input);

        assertEquals(100L, result.id());
        assertEquals("New Cat", result.title());
        verify(categoryDAO).addCategory(input);
    }

    @Test
    @DisplayName("Mock: addCategory validation")
    void addCategoryShouldValidateInput() {
        assertThrows(IllegalArgumentException.class, () -> categoryService.addCategory(null));
        assertThrows(IllegalArgumentException.class, () -> categoryService.addCategory(new Category(null, null, "")));
    }
}
