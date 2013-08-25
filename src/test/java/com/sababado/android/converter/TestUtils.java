package com.sababado.android.converter;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * Created by Robert on 8/24/13.
 */
public class TestUtils {

    final static String ROOT = "C:\\test";

    @Before
    public void init() {
        try {
            initDirectories();
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static void initDirectories() throws IOException {
        final String testProjResourceRoot = "testFolder/Test";
        FileUtils.forceMkdir(new File(ROOT + "/Dir Empty"));
        FileUtils.forceMkdir(new File(ROOT + "/Dir_one_string"));

        File dir = new File(ROOT + "/DirNotEmpty");
        FileUtils.forceMkdir(dir);
        Utils.copyFileFromResourcesToFile("testFolder/DirNotEmpty/help.properties", dir.getPath() + "/help.properties");
        dir = new File(ROOT + "/DirNotEmpty/Another Dir");
        FileUtils.forceMkdir(dir);
        Utils.copyFileFromResourcesToFile("testFolder/DirNotEmpty/Another Dir/some file.properties", dir.getPath() + "/some file.properties");

        final File testRoot = new File(ROOT + "/Test");
        FileUtils.forceMkdir(testRoot);
        FileUtils.forceMkdir(new File(testRoot.getPath() + "/assets"));
        FileUtils.forceMkdir(new File(testRoot.getPath() + "/bin"));
        FileUtils.forceMkdir(new File(testRoot.getPath() + "/gen"));
        FileUtils.forceMkdir(new File(testRoot.getPath() + "/libs"));
        FileUtils.forceMkdir(new File(testRoot.getPath() + "/res"));

        final File mainPackageDir = new File(testRoot.getPath() + "/src/com/example/test");
        FileUtils.forceMkdir(mainPackageDir);
        Utils.copyFileFromResourcesToFile(testProjResourceRoot + "/src/com/example/test/MainActivity.java", mainPackageDir.getPath() + "/MainActivity.java");
        Utils.copyFileFromResourcesToFile(testProjResourceRoot + "/.classpath", testRoot.getPath() + "/.classpath");
        Utils.copyFileFromResourcesToFile(testProjResourceRoot + "/.project", testRoot.getPath() + "/.project");
        Utils.copyFileFromResourcesToFile(testProjResourceRoot + "/AndroidManifest.xml", testRoot.getPath() + "/AndroidManifest.xml");
        Utils.copyFileFromResourcesToFile(testProjResourceRoot + "/ic_launcher-web.png", testRoot.getPath() + "/ic_launcher-web.png");
        Utils.copyFileFromResourcesToFile(testProjResourceRoot + "/proguard-project.txt", testRoot.getPath() + "/proguard-project.txt");
        Utils.copyFileFromResourcesToFile(testProjResourceRoot + "/project.properties", testRoot.getPath() + "/project.properties");

    }

    @After
    public void tearDown() {
        try {
            cleanupDirectories();
        } catch (IOException e) {
            System.out.println("Error cleaning up");
            e.printStackTrace();
        }
    }

    public static void cleanupDirectories() throws IOException {
        FileUtils.forceDelete(new File(ROOT));
    }

    @Test
    public void testMakeBackup() {
        File src = Utils.getDirectory("C:\\test\\Dir Empty", false);
        File copyDir = Utils.makeBackup(src);
        assertNotNull(copyDir);
        copyDir.delete();

        src = Utils.getDirectory("C:\\test\\Dir_one_string", false);
        copyDir = Utils.makeBackup(src);
        assertNotNull(copyDir);
        copyDir.delete();

        src = Utils.getDirectory("C:\\test\\DirNotEmpty", false);
        copyDir = Utils.makeBackup(src);
        assertNotNull(copyDir);
        try {
            FileUtils.deleteDirectory(copyDir);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDuplicateBackups() {
        File src = Utils.getDirectory("C:\\test\\Dir Empty", false);
        File copyDir = Utils.makeBackup(src);
        assertNotNull(copyDir);

        File copyDir2 = Utils.makeBackup(src);
        assertNotNull(copyDir);

        copyDir.delete();
        copyDir2.delete();
    }

    @Test
    public void testGetDirectory() {
        File dir = Utils.getDirectory("C:\\test\\Dir Empty", false);
        assertTrue(dir != null);
        assertTrue(dir.isDirectory());

        dir = Utils.getDirectory("C:\\test\\Dir_one_string", false);
        assertTrue(dir != null);
        assertTrue(dir.isDirectory());

        dir = Utils.getDirectory("C:\\test\\DirNotEmpty", false);
        assertTrue(dir != null);
        assertTrue(dir.isDirectory());

        String dirDoesntExist = "C:\\test\\DirDoesntExist";
        dir = Utils.getDirectory(dirDoesntExist, false);
        assertTrue(dir == null);

        dir = Utils.getDirectory(dirDoesntExist, true);
        assertTrue(dir != null);
        assertTrue(dir.isDirectory());
        dir.delete();
    }

    @Test
    public void testIDEFilter() {
        final File dir = Utils.getDirectory("C:\\test\\Test", false);
        assertNotNull(dir);

        final File[] files = dir.listFiles(Utils.IDE_FILTER);
        for (final File file : files) {
            final String name = file.getName();
            System.out.println("Asserting file: " + name);
            if (name.equals(".classpath") ||
                    name.equals(".project") ||
                    name.equals("local.properties") ||
                    name.equals("project.properties") ||
                    name.equals(".gradle") ||
                    name.equals(".iml") ||
                    name.equals("bin") ||
                    name.equals("gen")) {
                fail();
            }
        }
    }
}
