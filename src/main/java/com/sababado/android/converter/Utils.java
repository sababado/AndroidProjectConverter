package com.sababado.android.converter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Robert on 8/24/13.
 */
public class Utils {
    private static final String POST_FIX_BACKUP = "_apc_backup";

    /**
     * Types of dependencies.
     */
    public enum DependencyType {
            compile,instrumentTestCompile
    };

    /**
     * Helper method to copy a file from resources to an external location.
     *
     * @param resourcePath Path to the resource to copy
     * @param destPath     Path to the file to end up.
     * @return True if the operation is successful, false if there is an error.
     */
    public static boolean copyFileFromResourcesToFile(final String resourcePath, final String destPath) {
        System.out.println("Looking for resource: " + resourcePath);
        final InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath);
        final File fileDest = new File(destPath);
        try {
            FileUtils.copyInputStreamToFile(is, fileDest);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Get a directory with a given path.
     *
     * @param path                Path to get a directory by.
     * @param createIfDoesntExist True to create the directory if it doesn't exist, false to not.
     * @return A non null File object, or null if the path doesn't exist or it isn't a directory.
     */
    public static File getDirectory(final String path, final boolean createIfDoesntExist) {
        assert (path != null);
        File f = new File(path);
        if (!f.exists() && !createIfDoesntExist) {
            System.out.println("!! \"" + path + "\" does not exist.");
            return null;
        } else if (createIfDoesntExist) {
            try {
                FileUtils.forceMkdir(f);
            } catch (IOException e) {
                System.out.println("!! Could not make directory: " + path);
                return null;
            }
        } else if (!f.isDirectory()) {
            if (!createIfDoesntExist) {
                //return if it doesn't exist or can't create it.
                System.out.println("!! \"" + path + "\" is not a directory.");
                return null;
            }
        }
        return f;
    }

    /**
     * Make a backup of a given directory.
     *
     * @param sourceDir Directory to make a backup of.
     * @return Returns the directory if it was successfully backed-up. Null if there was an error.
     */
    public static File makeBackup(final File sourceDir) {
        assert (sourceDir != null);
        //make a backup.
        final File copyDir = getBackupDirectory(sourceDir, POST_FIX_BACKUP);
        try {
            FileUtils.copyDirectory(sourceDir, copyDir);
        } catch (IOException e) {
            System.out.println("Could not make a backup");
            e.printStackTrace();
            return null;
        }
        return copyDir;
    }

    /**
     * Get the directory to backup to. The directory will be next to the source directory and have the post fix concatenated to it.
     *
     * @param sourceDir   Directory backup from.
     * @param destPostFix Post fix to append to the directory name.
     * @return Return the directory that should be used as a backup, null if there is an error.
     */
    public static File getBackupDirectory(final File sourceDir, String destPostFix) {
        assert (sourceDir != null);
        assert (sourceDir.isDirectory());

        if (destPostFix == null)
            destPostFix = "";

        //get copy directory
        String copyDirPath = sourceDir.getPath().concat(destPostFix);
        final String baseCopyDirPath = copyDirPath;
        File copyDir = new File(copyDirPath);
        int counter = 1;
        //don't use one that already exists.
        while (copyDir.exists()) {
            copyDir = new File(baseCopyDirPath + String.valueOf(counter));
            System.out.println("New backup destination: " + copyDir.getPath());
            counter++;
        }
        //make the directory
        try {
            FileUtils.forceMkdir(copyDir);
        } catch (IOException e) {
            System.out.println("Could not make the directory: " + copyDirPath);
            return null;
        }
        return copyDir;
    }

    /**
     * Get an argument from the args array.
     *
     * @param args         Arguments to get from
     * @param defaultIndex Default index in the case of no preceding optional parameters
     * @param indexOffset  Index offset in the case of any preceding optional parameters.
     * @return An argument as a string.
     * @throws IndexOutOfBoundsException Thrown if there indices are off.
     */
    public static String getArgument(final String[] args, final int defaultIndex, final int indexOffset) throws IndexOutOfBoundsException {
        return args[defaultIndex + indexOffset];
    }

    /**
     * File filer. Filters any IDE specific files (Eclipse, or Intellij) as well as environment local files.
     */
    public static final FileFilter IDE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            final String name = pathname.getName();
            if (name.equals(".classpath") ||
                    name.equals(".project") ||
                    name.equals("project.properties") ||
                    name.equals("local.properties") ||
                    name.equals(".gradle") ||
                    name.equals(".iml") ||
                    name.equals("bin") ||
                    name.equals("gen")) {
                return false;
            }
            return true;
        }
    };
}
