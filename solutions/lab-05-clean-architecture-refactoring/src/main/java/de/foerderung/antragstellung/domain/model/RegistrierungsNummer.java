package de.foerderung.antragstellung.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a registration number for an application.
 * Format: DZ-XX-YYYY-NNNN (e.g. DZ-BW-2024-0042)
 */
public record RegistrierungsNummer(String wert) {

    private static final Pattern FORMAT = Pattern.compile("DZ-[A-Z]{2}-\\d{4}-\\d{4}");

    public RegistrierungsNummer {
        Objects.requireNonNull(wert, "RegistrierungsNummer darf nicht null sein");
        if (!FORMAT.matcher(wert).matches()) {
            throw new IllegalArgumentException(
                "RegistrierungsNummer muss dem Format DZ-XX-YYYY-NNNN entsprechen: " + wert);
        }
    }
}
