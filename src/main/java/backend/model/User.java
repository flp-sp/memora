package backend.model;

public record User(
    Long id,
    String userName,
    String email,
    String passHash,
    char sysRole
) {}
