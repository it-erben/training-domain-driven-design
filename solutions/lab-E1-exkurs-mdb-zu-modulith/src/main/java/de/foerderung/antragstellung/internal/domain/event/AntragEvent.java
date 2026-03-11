package de.foerderung.antragstellung.internal.domain.event;

public sealed interface AntragEvent permits
        FlurstueckHinzugefuegt, NachweisEingereicht,
        NachweisAkzeptiert, InternalAntragsmappeEingereicht {
}
