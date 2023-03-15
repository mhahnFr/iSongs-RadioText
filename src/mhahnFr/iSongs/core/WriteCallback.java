package mhahnFr.iSongs.core;

import mhahnFr.utils.Pair;

@FunctionalInterface
public interface WriteCallback {
    void songWritten(final Pair<String, String> song, final Exception exception);
}
