package service;

import dao.ChildDAO;
import dao.exception.DatabaseException;
import model.Child;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Child operations.
 * Handles business logic, input validation, and exception translation.
 */
public class ChildService {
    private final ChildDAO childDAO;

    public ChildService(ChildDAO childDAO) {
        if (childDAO == null) {
            throw new IllegalArgumentException("ChildDAO cannot be null");
        }
        this.childDAO = childDAO;
    }

    /**
     * Adds a new child.
     *
     * @param child the child to add
     * @return the added child
     * @throws IllegalArgumentException if child is null
     * @throws DatabaseException if a database error occurs
     */
    public Child addChild(Child child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null");
        }
        try {
            return childDAO.addChild(child);
        } catch (SQLException e) {
            throw new DatabaseException("Error adding child: " + child.firstName() + " " + child.lastName(), e);
        }
    }

    /**
     * Updates an existing child's information.
     *
     * @param child the child info to update
     * @return true if update was successful
     * @throws IllegalArgumentException if child or child ID is null
     * @throws DatabaseException if a database error occurs or child not found
     */
    public boolean updateChild(Child child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null");
        }
        if (child.id() == null) {
            throw new IllegalArgumentException("Child ID cannot be null for update");
        }
        try {
            boolean updated = childDAO.updateChild(child);
            if (!updated) {
                throw new DatabaseException("Child with ID " + child.id() + " not found or could not be updated");
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating child with ID: " + child.id(), e);
        }
    }

    /**
     * Deletes a child by ID.
     *
     * @param id the ID of the child to delete
     * @return true if deletion was successful
     * @throws IllegalArgumentException if id is null
     * @throws DatabaseException if a database error occurs
     */
    public boolean deleteChild(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Child ID cannot be null");
        }
        try {
            boolean deleted = childDAO.deleteChild(id);
            if (!deleted) {
                throw new DatabaseException("Child with ID " + id + " not found or could not be deleted");
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting child with ID: " + id, e);
        }
    }

    /**
     * Finds children who are at least a certain age.
     *
     * @param minAge the minimum age
     * @return list of children
     * @throws IllegalArgumentException if minAge is negative
     * @throws DatabaseException if a database error occurs
     */
    public List<Child> findOlderChildren(int minAge) {
        if (minAge < 0) {
            throw new IllegalArgumentException("Minimum age cannot be negative");
        }
        try {
            return childDAO.findChildrenWithMinimumAge(minAge);
        } catch (SQLException e) {
            throw new DatabaseException("Error finding children with minimum age: " + minAge, e);
        }
    }

    /**
     * Finds children who don't have a birth date specified.
     *
     * @return list of children
     * @throws DatabaseException if a database error occurs
     */
    public List<Child> findChildrenMissingBirthDate() {
        try {
            return childDAO.findChildrenWithoutBirthDate();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding children without birth date", e);
        }
    }
}
