package de.foerderung.auswertung.internal.domain.model;

/**
 * Enum representing the monitoring status in the Auswertung context.
 * This is the BC's OWN status — mapped from AenderungsArt via the ACL Translator.
 */
public enum MonitoringsStatus {
    AKTIV,
    INAKTIV,
    ARCHIVIERT
}
