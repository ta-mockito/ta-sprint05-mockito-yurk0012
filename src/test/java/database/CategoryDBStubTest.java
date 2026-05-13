package database;

import dao.CategoryDAO;
import model.Category;
import model.Child;
import service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Stub demo for category flow")
class CategoryDBStubTest {

    private CategoryDAO stubDao;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        stubDao = new InMemoryCategoryDAOStub();
        categoryService = new CategoryService(stubDao);
    }

    @Test
    @DisplayName("Stub: search returns deterministic in-memory data")
    void searchShouldReturnExpectedItemsFromStub()  {
        List<Category> result = categoryService.searchCategories("sport");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.title().toLowerCase().contains("sport")));
    }

    @Test
    @DisplayName("Stub: rename updates item in the in-memory store")
    void renameShouldUpdateCategoryInStub()  {
        boolean updated = categoryService.renameCategory(1L, "Updated Sports");
        List<Category> result = categoryService.searchCategories("updated");

        assertTrue(updated);
        assertEquals(1, result.size());
        assertEquals("Updated Sports", result.get(0).title());
    }

    @Test
    @DisplayName("Stub: delete returns false for unknown id")
    void deleteShouldReturnFalseForUnknownId() throws SQLException {
        boolean deleted = stubDao.deleteCategory(999L);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("Stub: child list is fixed for a category")
    void childrenShouldComeFromStubData() {
        int childCount = categoryService.countChildren(2L);

        assertEquals(2, childCount);
    }

    static class InMemoryCategoryDAOStub implements CategoryDAO {
        private final Map<Long, Category> categories = new HashMap<>();
        private final Map<Long, List<Child>> childrenByCategory = new HashMap<>();

        InMemoryCategoryDAOStub() {
            categories.put(1L, new Category(1L, "a1", "Sports"));
            categories.put(2L, new Category(2L, "a2", "Esports"));
            categories.put(3L, new Category(3L, "a3", "Science"));

            childrenByCategory.put(1L, List.of(new Child(1L, "John", "Doe", LocalDate.of(2014, 1, 1))));
            childrenByCategory.put(2L, List.of(
                    new Child(2L, "Emma", "Stone", null),
                    new Child(3L, "Alex", "Green", LocalDate.of(2013, 6, 10))
            ));
            childrenByCategory.put(3L, List.of());
        }

        @Override
        public Category addCategory(Category category) {
            long nextId = categories.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
            Category created = new Category(nextId, category.avatar(), category.title());
            categories.put(nextId, created);
            return created;
        }

        @Override
        public boolean updateCategory(Category category) {
            if (!categories.containsKey(category.id())) {
                return false;
            }
            categories.put(category.id(), category);
            return true;
        }

        @Override
        public boolean deleteCategory(Long id) {
            return categories.remove(id) != null;
        }

        @Override
        public List<Category> findCategoriesByTitlePart(String titlePart) {
            String lowered = titlePart.toLowerCase();
            return categories.values().stream()
                    .filter(c -> c.title().toLowerCase().contains(lowered))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Child> findAllChildrenInCategory(Long categoryId) {
            return new ArrayList<>(childrenByCategory.getOrDefault(categoryId, List.of()));
        }
    }
}





















