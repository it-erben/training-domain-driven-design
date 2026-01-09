package de.realestate.brokerage.application.command;

import java.util.UUID;

public record CompleteViewingCommand(
        UUID processId,
        UUID viewingId
) {}
