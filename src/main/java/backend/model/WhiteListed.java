package backend.model;

public record WhiteListed(
    String email,
    char currentStatus,
    char sysRole
) {}