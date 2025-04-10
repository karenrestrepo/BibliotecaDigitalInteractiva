package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

public class Rating {
    private Book book;
    private int stars;
    private String comment;

    public Rating(Book book, int stars, String comment) {
        this.book = book;
        this.stars = stars;
        this.comment = comment;
    }

    public Book getBook() { return book; }
    public int getStars() { return stars; }
    public String getComment() { return comment; }

    @Override
    public String toString() {
        return "Rating: " + stars + " stars for \"" + book.getTitle() + "\" - " + comment;
    }
}
