package de.foerderung.antragstellung.internal.domain.event;

/**
 * Sealed interface for all domain events in the Antragstellung bounded context.
 */
public sealed interface AntragEvent permits
        FlurstueckHinzugefuegt, NachweisEingereicht,
        NachweisAkzeptiert, InternalAntragsmappeEingereicht {
}
