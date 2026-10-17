package backend.model;

import java.time.LocalDateTime;

// alias de register no banco
public record Register(
    long id,
    long idUser,
    long idPrisoner,
    LocalDateTime dateRegister,
    String description,
    String observations,
    long idUserEdit,
    String userName,
    String userNameEdit
) {}