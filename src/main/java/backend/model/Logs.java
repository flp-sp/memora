package backend.model;

import java.time.LocalDateTime;

public record Logs(
    Long id,
    Long idUser,
    Long idPrisoner,
    String whitelistedEmail,
    String description,
    LocalDateTime dateTime, 
    Long idRegister
) {}