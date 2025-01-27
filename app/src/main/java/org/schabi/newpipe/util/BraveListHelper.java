package org.schabi.newpipe.util;

import androidx.annotation.NonNull;

public final class BraveListHelper {

    private BraveListHelper() {

    }

    static String extractResolution(@NonNull final String resolution,
                                    final String resolutionNoRefresh) {
        if (resolution.equals(resolutionNoRefresh)) {
            // Assume Rumble Service -> remove bitrate from eg 1080p@3343k.
            // Rumble uses Bitrate to differentiate multiple same resolution videos.
            // As NewPipe  only checks for matching resolution we covert string back to
            // resolution only: 1080p
            return resolution.replaceAll("p@\\d+k", "p");
        }
        return resolution;
    }
}
