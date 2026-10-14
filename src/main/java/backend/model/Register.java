package backend.model;

import java.time.LocalDate;

// alias de register no banco
public record Register(
    long id,
    long idUser,
    long idPrisoner,
    LocalDate dateRegister,
    String description,
    String observations
) {}