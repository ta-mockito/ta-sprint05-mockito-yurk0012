package model;

public record Category(Long id, String avatar, String title) {
    public Category(String avatar, String title) {
        this(null, avatar, title);
    }
}


