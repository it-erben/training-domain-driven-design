package de.foerderung.antragstellung.domain.event;

/**
 * Sealed interface for all domain events in the Antragstellung bounded context.
 */
public sealed interface AntragEvent permits
        AntragsmappeErstellt, FlurstueckHinzugefuegt,
        NachweisEingereicht, NachweisAkzeptiert,
        AntragsmappeEingereicht {
}
