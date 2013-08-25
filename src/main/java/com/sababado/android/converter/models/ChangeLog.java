package com.sababado.android.converter.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Robert on 8/25/13.
 */
public class ChangeLog {

    public ArrayList<Update> updates;

    /**
     * Sort the list of updates
     *
     * @return
     */
    public void sort() {
        Collections.sort(updates);
    }

    /**
     * Check to see if there is a more recent update than the given update.
     *
     * @param version update to compare with
     * @return The more recent update is returned. null if there is none.
     */
    public Update containsMoreRecentUpdate(Update version) {
        assert (version != null);
        if (updates != null) {
            for (final Update update : updates) {
                if (update.compareTo(version) == -1) {
                    return update;
                }
            }
        }
        return null;
    }

    public static class Update implements Comparable<Update> {
        public String version;
        public String description;
        public Date updatedAt;

        public Update() {
        }

        public Update(final String version) {
            this.version = version;
        }

        @Override
        public int compareTo(Update o) {
            //sort in descending order
            if (o == null)
                return -1;

            assert (version != null);
            assert (o.version != null);

            final String[] splitVersion = version.split("\\.");
            final String[] splitOtherVersion = o.version.split("\\.");

            final int minSize = Math.min(splitVersion.length, splitOtherVersion.length);
            for (int i = 0; i < minSize; i++) {
                final int thisVersion = Integer.parseInt(splitVersion[i]);
                final int otherVersion = Integer.parseInt(splitOtherVersion[i]);
                if (thisVersion > otherVersion)
                    return -1;
                else if (thisVersion < otherVersion)
                    return 1;
            }
            return 0;
        }

        @Override
        public String toString() {
            Calendar c = Calendar.getInstance();
            c.setTime(updatedAt);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            return "v" + version + " updated on " + format1.format(c.getTime()) + "\n" + description;
        }
    }
}
