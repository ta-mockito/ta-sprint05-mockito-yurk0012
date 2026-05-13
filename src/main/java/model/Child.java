package model;

import java.time.LocalDate;

public record Child(Long id, String firstName, String lastName, LocalDate birthDate) {
    public Child(String firstName, String lastName, LocalDate birthDate) {
        this(null, firstName, lastName, birthDate);
    }
}
