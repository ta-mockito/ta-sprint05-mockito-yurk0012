package service;

import dao.CategoryDAO;
import dao.exception.DatabaseException;
import model.Category;
import model.Child;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Category operations.
 * Handles business logic, input validation, and exception translation.
 */
public class CategoryService {
    private final CategoryDAO categoryDAO;

    public CategoryService(CategoryDAO categoryDAO) {
        if (categoryDAO == null) {
            throw new IllegalArgumentException("CategoryDAO cannot be null");
        }
        this.categoryDAO = categoryDAO;
    }

    /**
     * Searches for categories by title part.
     *
     * @param titlePart the part of the title to search for
     * @return a list of matching categories
     * @throws IllegalArgumentException if titlePart is null or empty
     * @throws DatabaseException if a database error occurs
     */
    public List<Category> searchCategories(String titlePart) {
        if (titlePart == null || titlePart.isBlank()) {
            throw new IllegalArgumentException("Title part cannot be null or empty");
        }
        try {
            return categoryDAO.findCategoriesByTitlePart(titlePart.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Error searching categories by title part: " + titlePart, e);
        }
    }

    /**
     * Renames an existing category.
     *
     * @param id the ID of the category to rename
     * @param newTitle the new title for the category
     * @return true if the rename was successful
     * @throws IllegalArgumentException if id is null or newTitle is null/blank
     * @throws DatabaseException if a database error occurs or category is not found
     */
    public boolean renameCategory(Long id, String newTitle) {
        if (id == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException("New title cannot be null or blank");
        }

        try {
            Category categoryToUpdate = new Category(id, null, newTitle.trim());
            boolean updated = categoryDAO.updateCategory(categoryToUpdate);
            if (!updated) {
                throw new DatabaseException("Category with ID " + id + " not found or could not be updated");
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error renaming category with ID: " + id, e);
        }
    }

    /**
     * Counts the number of children in a given category.
     *
     * @param categoryId the ID of the category
     * @return the count of children
     * @throws IllegalArgumentException if categoryId is null
     * @throws DatabaseException if a database error occurs
     */
    public int countChildren(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        try {
            List<Child> children = categoryDAO.findAllChildrenInCategory(categoryId);
            if (children == null) {
                return 0;
            }
            return children.size();
        } catch (SQLException e) {
            throw new DatabaseException("Error counting children for category ID: " + categoryId, e);
        }
    }

    /**
     * Adds a new category or returns existing if found by title.
     *
     * @param category the category to add
     * @return the added or existing category
     * @throws IllegalArgumentException if category is null or has invalid data
     * @throws DatabaseException if a database error occurs
     */
    public Category addCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (category.title() == null || category.title().isBlank()) {
            throw new IllegalArgumentException("Category title cannot be null or blank");
        }
        
        try {
            return categoryDAO.addCategory(category);
        } catch (SQLException e) {
            throw new DatabaseException("Error adding category: " + category.title(), e);
        }
    }
}
