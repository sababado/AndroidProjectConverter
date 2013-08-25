package com.sababado.android.converter;

import com.google.gson.Gson;
import com.sababado.android.converter.models.ChangeLog;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Created by Robert on 8/25/13.
 */
public class TestChangeLog {

    @Test
    public void testGetChangeLogSort() {
        ChangeLog changeLog = (new Gson()).fromJson(CHANGELOG, ChangeLog.class);
        assertEquals(3, changeLog.updates.size());
        changeLog.sort();
        ChangeLog.Update lastUpdate = null;
        for (final ChangeLog.Update update : changeLog.updates) {
            if (lastUpdate != null) {
                System.out.println(update.version + " / " + lastUpdate.version);
                assertEquals(1, update.compareTo(lastUpdate));
            }
            lastUpdate = update;
        }
    }

    @Test
    public void testChangeLogContainsUpdate() {
        ChangeLog changeLog = (new Gson()).fromJson(CHANGELOG_UPDATE, ChangeLog.class);
        ChangeLog.Update update = new ChangeLog.Update("1.2.0");
        ChangeLog.Update moreRecentUpdate = changeLog.containsMoreRecentUpdate(update);

        assertNotNull(moreRecentUpdate);
        assertEquals("1.3.1", moreRecentUpdate.version);

        update = new ChangeLog.Update("1.3.0");
        moreRecentUpdate = changeLog.containsMoreRecentUpdate(update);
        assertNotNull(moreRecentUpdate);
        assertEquals("1.3.1", moreRecentUpdate.version);

        update = new ChangeLog.Update("1.4.0");
        moreRecentUpdate = changeLog.containsMoreRecentUpdate(update);
        assertNull(moreRecentUpdate);
    }

    private static final String CHANGELOG = "{\"updates\":[{\"version\":\"1.2.0\",\"description\":\"Added the ability to copy an Android Test Project simultaneously with an Android Project.\",\"updatedAt\":\"2013-08-25T21:00:00Z\"},{\"version\":\"1.1.0\",\"description\":\"Added support to modify Module build.gradle file with all dependencies in a lib(s) directory.\",\"updatedAt\":\"2013-08-25T17:00:00Z\"},{\"version\":\"1.0.0\",\"description\":\"Initial Release\",\"updatedAt\":\"2013-08-25T05:00:00Z\"}]}";
    private static final String CHANGELOG_UPDATE = "{\"updates\":[{\"version\":\"1.3.1\",\"description\":\"New update\",\"updatedAt\":\"2013-08-25T21:01:00Z\"},{\"version\":\"1.2.0\",\"description\":\"Added the ability to copy an Android Test Project simultaneously with an Android Project.\",\"updatedAt\":\"2013-08-25T21:00:00Z\"},{\"version\":\"1.1.0\",\"description\":\"Added support to modify Module build.gradle file with all dependencies in a lib(s) directory.\",\"updatedAt\":\"2013-08-25T17:00:00Z\"},{\"version\":\"1.0.0\",\"description\":\"Initial Release\",\"updatedAt\":\"2013-08-25T05:00:00Z\"}]}";
}
