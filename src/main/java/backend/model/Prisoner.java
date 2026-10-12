package backend.model;

public record Prisoner(
    String lawsuitNum,
    String prisonerName,
    String dutyDate,
    String sentenceType,
    String dateLastAnalysis,
    String dateLastService,
    String dateTransferApac,
    String jail
) {}
