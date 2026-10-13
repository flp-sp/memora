package backend.model;

import java.time.LocalDate;

public record Prisoner(
    long id,
    String lawsuitNum,
    String prisonerName,
    LocalDate dutyDate,
    char sentenceType,
    LocalDate dateLastAnalysis,
    LocalDate dateLastService,
    LocalDate dateTransferApac,
    char jail
) {}
