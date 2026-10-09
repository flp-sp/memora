package backend.model;

public record User(
    long id,
    String userName,
    String email,
    String passHash
) {}
