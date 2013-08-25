package com.sababado.android.converter;


import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class TestMain {

    @Before
    public void init() {
        try {
            TestUtils.initDirectories();
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    @After
    public void tearDown() {
        try {
            TestUtils.cleanupDirectories();
        } catch (IOException e) {
            System.out.println("Error cleaning up");
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateAndroidStudioProjectShell() {
        File dir = Utils.getDirectory("C:\\test\\TestShell", true);
        File testDir = Utils.getDirectory("C:\\test\\TestShell\\TestDir", true);
        try {
            AndroidProjectConverter.createAndroidStudioShellProject(dir, "Name", testDir);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertNotNull(Utils.getDirectory("C:\\test\\TestShell\\gradle\\wrapper", false));
        File file = new File("C:\\test\\TestShell\\gradle\\wrapper\\gradle-wrapper.jar");
        assertTrue(file.exists());
        file = new File("C:\\test\\TestShell\\gradle\\wrapper\\gradle-wrapper.properties");
        assertTrue(file.exists());

        file = new File("C:\\test\\TestShell\\build.gradle");
        assertTrue(file.exists());
        file = new File("C:\\test\\TestShell\\settings.gradle");
        assertTrue(file.exists());

        try {
            FileUtils.forceDelete(dir);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not delete directory: "+dir.getPath());
        }
    }

    @Test
    public void testCreateAndroidStudioModule() {
        File parentDir = Utils.getDirectory("C:\\test\\TestShell", true);
        try {
            AndroidProjectConverter.createAndroidStudioModule(parentDir, "TestModule", false);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        final File module = commonModuleAssert(parentDir);
        final File dir = Utils.getDirectory(module.getPath()+"/src/instrumentTest",false);
        assertNull(dir);

        try {
            FileUtils.forceDelete(parentDir);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not delete directory: "+parentDir.getPath());
        }
    }

    @Test
    public void testCreateAndroidStudioModuleWithTests() {
        File parentDir = Utils.getDirectory("C:\\test\\TestShell", true);
        try {
            AndroidProjectConverter.createAndroidStudioModule(parentDir, "TestModule", true);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        final File module = commonModuleAssert(parentDir);
        final File dir = Utils.getDirectory(module.getPath()+"/src/instrumentTest/java",false);
        assertNotNull(dir);
        assertTrue(dir.exists());

        try {
            FileUtils.forceDelete(parentDir);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not delete directory: "+parentDir.getPath());
        }
    }

    private File commonModuleAssert(final File parentDir) {
        File module = Utils.getDirectory(parentDir.getPath()+"/TestModule",false);
        assertTrue(module.exists());

        File dir = Utils.getDirectory(module.getPath()+"/src/main/java",false);
        assertNotNull(dir);
        assertTrue(dir.exists());
        dir = Utils.getDirectory(module.getPath()+"/src/main/res",false);
        assertNotNull(dir);
        assertTrue(dir.exists());

        File file = new File(module.getPath()+"/build.gradle");
        assertTrue(file.exists());
        return module;
    }
}
