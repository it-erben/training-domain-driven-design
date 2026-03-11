package de.foerderung.antragstellung;

/**
 * Published Language: Describes the kind of change that occurred on an AntragsMappe.
 * Part of the public module API — consumed by other bounded contexts via ACL.
 */
public enum AenderungsArt {
    AKTUALISIERT,
    REAKTIVIERT,
    ENTFERNT,
    ARCHIVIERT
}
