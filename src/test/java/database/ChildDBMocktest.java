package database;


import dao.ChildDB;
import model.Child;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mock for ChildDBMockTest")


public class ChildDBMocktest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement pst;
    @Mock
    private ResultSet rs;

    private ChildDB childDB;

    @BeforeEach
    void setUp() {
        childDB = new ChildDB(connection);
    }

    @Test
    @DisplayName("Should add a child and return it with an ID")
    void addChildShouldSaveAndReturnChildWithId() throws SQLException {
        // 1. Задаєш поведінку моків
        when(connection.prepareStatement(anyString(),
                eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(pst);
        when(pst.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(100L);

        // 2. Викликаєш метод
        Child saved = childDB.addChild(
                new Child(null, "John", "Doe", LocalDate.of(2015, 6, 15))
        );

        // 3. Перевіряєш результат
        assertNotNull(saved.id());
        assertEquals(100L, saved.id());
        assertEquals("John", saved.firstName());
        verify(pst).executeUpdate(); // перевіряє що executeUpdate був викликаний
    }


    @Test
    @DisplayName("Should add a child with null birth date")
    void addShouldHandleNullBirthDate() throws SQLException {
        Child child = new Child("Doe", "John", null);


        when(connection.prepareStatement(anyString(),
                eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(pst);
        when(pst.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(100L);

        Child addedChild = childDB.addChild(child);


        assertNotNull(addedChild.id(), "Child ID should not be null");
        assertNull(addedChild.birthDate(), "Birth date should be null");
        assertEquals("Doe", addedChild.firstName());

    }

    @Test
    @DisplayName("Should update an existing child")
    void updateChildShouldReturnTrue() throws SQLException {
        Child uptChild = new Child(1L, "John", "Doe1", LocalDate.of(2015, 6, 15));

        when(connection.prepareStatement(anyString())).thenReturn(pst);
        when(pst.executeUpdate()).thenReturn(1);

        boolean result = childDB.updateChild(uptChild);

        assertTrue(result);
    }


    @Test
    @DisplayName("Should delete an existing child")
    void deleteShouldDeleteExistingChild() throws SQLException {
        Child exchild = new Child(1L, "John", "Doe1", LocalDate.of(2015, 6, 15));
        when(connection.prepareStatement(anyString())).thenReturn(pst);
        when(pst.executeUpdate()).thenReturn(1  );

        boolean result = childDB.deleteChild(exchild.id());

        assertTrue(result);

    }

    @Test
    @DisplayName("Should return children with at least the specified age")
    void findChildrenWithMinimumAgeShouldReturnChildrenWithMinimumAge() throws SQLException {
        int minAge = 10;

        when (connection.prepareStatement(anyString())).thenReturn(pst);
        when(pst.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true,true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("first_name")).thenReturn("Ten", "Fifteen");
        when(rs.getString("last_name")).thenReturn("YearOld", "YearOld");
        when(rs.getDate("birth_date")).thenReturn(
                Date.valueOf(LocalDate.now().minusYears(10)),
                Date.valueOf(LocalDate.now().minusYears(15)));

        List<Child> children = childDB.findChildrenWithMinimumAge(minAge);
        assertEquals(2, children.size());
        assertEquals("Ten", children.get(0).firstName());
        assertEquals("Fifteen", children.get(1).firstName());

    }

    @Test
    @DisplayName("Should return children with null birth date")
    void findChildrenWithoutBirthDateShouldReturnChildrenWithNullBirthDate() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(pst);
        when(pst.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true ,  false);
        when(rs.getLong("id")).thenReturn(100L);
        when(rs.getString("first_name")).thenReturn("Ten");
        when(rs.getString("last_name")).thenReturn("Fifteen");
        when(rs.getDate("birth_date")).thenReturn(null);

        List <Child> children = childDB.findChildrenWithoutBirthDate();

        assertEquals(1, children.size());
        assertEquals("Ten", children.get(0).firstName());
        assertEquals("Fifteen", children.get(0).lastName());
        assertNull(children.get(0).birthDate());
    }

    @Test
    @DisplayName("Should throw exception when adding null child")
    void addChildShouldThrowExceptionForNullChild(){

        assertThrows(IllegalArgumentException.class, () -> childDB.addChild(null));
        verifyNoInteractions(connection);


    }
    @Test
    @DisplayName("Should throw exception when adding child with empty first name")
    void addChildShouldThrowExceptionForEmptyFirstName() {
        Child child = new Child("", "Doe", LocalDate.of(2015, 6, 15));
        assertThrows(IllegalArgumentException.class, () -> childDB.addChild(child));
        verifyNoInteractions(connection);
    }
    @Test
    @DisplayName("Should throw exception when updating null child")
    void updateChildShouldThrowExceptionForNullChild() {

        assertThrows(IllegalArgumentException.class, () ->childDB.updateChild(null));

        verifyNoInteractions(connection);
    }

    @Test
    @DisplayName("Should throw exception when deleting with null ID")
    void deleteChildShouldThrowExceptionForNullId() {

        assertThrows(IllegalArgumentException.class, () -> childDB.deleteChild(null));

        verifyNoInteractions(connection);
    }

    @Test
    @DisplayName("Should throw exception for negative age")
    void findChildrenWithMinimumAgeShouldThrowExceptionForNegativeAge() {

        assertThrows(IllegalArgumentException.class, () -> childDB.findChildrenWithMinimumAge(-1));

        verifyNoInteractions(connection);
    }
}

