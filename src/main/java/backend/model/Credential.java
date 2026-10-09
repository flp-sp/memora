package backend.model;

public record Credential(
    User user,
    String senhaHash
){}