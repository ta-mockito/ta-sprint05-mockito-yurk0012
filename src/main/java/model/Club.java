package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Club {

    private Long id;
    private String title;
    private Category category;
    private String description;
    private String imageUrl;

    public Club(String title, Category category, String description, String imageUrl) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
