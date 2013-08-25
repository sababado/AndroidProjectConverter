package com.sababado.android.converter;

import com.sababado.android.converter.models.ChangeLog;
import com.sababado.android.converter.network.ChangeLogService;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

public final class AndroidProjectConverter {

    private static final String VERSION = "1.3.0";
    private static final String TASK_UPDATE = "/u";
    private static final String DEPENDENCY_PLACE_HOLDER = "//TODO Put module dependencies here";
    private static final int DEFAULT_INDEX_SOURCE = 0;
    private static final int DEFAULT_INDEX_DESTINATION = DEFAULT_INDEX_SOURCE + 1;
    private static final int DEFAULT_INDEX_NAME = DEFAULT_INDEX_DESTINATION + 1;
    private static final int DEFAULT_INDEX_SOURCE_TEST = DEFAULT_INDEX_NAME + 1;

    public static void main(String[] args) {
        //Make sure there are some arguments
        if (args == null || args.length == 0) {
            printHelp();
            return;
        }
        System.out.println("Got arguments: " + args.length);
        /*
         * Check for updates if the user explicitly asks for it, or always check before starting work.
         */
        final boolean hasUpdateTask = args[0].equals(TASK_UPDATE);
        final boolean isRequestingUpdate = hasUpdateTask || args.length > 1;
        final ChangeLog.Update update;
        if (isRequestingUpdate) {
            final ChangeLog.Update thisVersion = new ChangeLog.Update(VERSION);
            update = checkForUpdates(thisVersion);
            if(update != null) {
                System.out.println("There is an update available! "+update.version);
                System.out.println(update);
            }
            if (args.length == 1) {
                //Return because the user is only requesting an update check.
                return;
            }
        } else {
            update = null;
        }

        System.out.println("Getting arg values");
        //get the source and destination strings
        final int offset = hasUpdateTask ? 1 : 0;
        final String source;
        final String dest;
        final String name;
        String sourceTest;
        try {
            source = Utils.getArgument(args, DEFAULT_INDEX_SOURCE, offset).trim();
            System.out.println("Got source: " + source);
            dest = Utils.getArgument(args, DEFAULT_INDEX_DESTINATION, offset).trim();
            System.out.println("Got dest: " + dest);
            name = Utils.getArgument(args, DEFAULT_INDEX_NAME, offset).trim();
            System.out.println("Got name: " + name);
        } catch (IndexOutOfBoundsException e) {
            //If this happens then that means the arguments weren't setup properly.
            printHelp();
            return;
        }
        try {
            sourceTest = Utils.getArgument(args, DEFAULT_INDEX_SOURCE_TEST, offset).trim();
            System.out.println("Got test project source: " + sourceTest);
        } catch (IndexOutOfBoundsException e) {
            sourceTest = null;
            System.out.println("No test project set");
        }
        System.out.println("Converting project");
        convertProject(source, dest, name, sourceTest);

        if(update != null) {
            System.out.println("Reminder that there is an update available.");
            System.out.println(update);
        }
    }

    /**
     * Check to see if this program has a new update.
     *
     * @return Returns true if there is an update, false if no update.
     */
    public static ChangeLog.Update checkForUpdates(final ChangeLog.Update thisVersion) {
        System.out.println("Checking for updates...");
        try {
            RestAdapter restAdapter = new RestAdapter.Builder().setServer("http://sababado.github.io/AndroidProjectConverter").build();
            ChangeLogService changeLogService = restAdapter.create(ChangeLogService.class);
            ChangeLog changeLog = changeLogService.getChangeLog();
            changeLog.sort();
            return changeLog.containsMoreRecentUpdate(thisVersion);
        } catch (RetrofitError e) {
            System.out.println("Error!! Could not check for updates at this time.");
            return null;
        }
    }

    public static void printHelp() {
        final StringBuilder sb = new StringBuilder();

        sb.append("Android Project Converter v" + VERSION);

        sb.append("\nUsage:\tapc [/u] <source_project_path> <destination_project_path> <name> [source_test_project]\n\n");
        sb.append("[/u]\tThe program will always check for updates before running, however use this flag to only check for updates. In this case the other values aren't necessary.\n");
        sb.append("<source>\tThis is the path of the project that needs to be converted\n");
        sb.append("<destination>\tThis is the path to the root directory of the converted project.\n\n");
        sb.append("<name>\tThis is the name of the project.\n");
        sb.append("[source_test_project]\tPath to the Android Test Project.");
        sb.append("A backup of the source project is automatically created. It will be copied to <source>-apc_backup");
        System.out.println(sb.toString());
    }

    /**
     * Convert a project with a valid source directory and destination directory
     *
     * @param source     Source directory
     * @param dest       Destination directory
     * @param name       Name of the project
     * @param sourceTest Directory of the Test project.
     */
    public static void convertProject(final String source, final String dest, final String name, final String sourceTest) {
        assert (source != null);
        assert (dest != null);
        assert (name != null);
        if (name.length() < 1) {
            System.out.println("A valid name must be given.");
        }

        final File sourceDir = Utils.getDirectory(source, false);
        if (sourceDir == null)
            return;
        File destDir = Utils.getDirectory(dest, false);
        if (destDir != null) {
            System.out.println("The destination project already exists, not overwriting.");
            System.out.println("Quitting.");
            return;
        }
        destDir = Utils.getDirectory(dest, true);
        if (destDir == null)
            return;

        final File sourceTestDir;
        if (sourceTest != null) {
            sourceTestDir = new File(sourceTest);
        } else {
            sourceTestDir = null;
        }


        System.out.println("Directories are valid");

        //make a backup.
        if (Utils.makeBackup(sourceDir) == null)
            return;

        //create android studio shell project
        try {
            createAndroidStudioShellProject(destDir, name, sourceTestDir);
        } catch (IOException e) {
            System.out.println("There was an error creating the android project shell");
            e.printStackTrace();
            return;
        }

        //copy project files
        if (copyProjectFiles(sourceDir, new File(destDir.getPath() + "/" + name), sourceTestDir)) {
            System.out.println("\n------------------------------");
            System.out.println("Project converted Successfully!!");
            System.out.println("------------------------------");
        } else {
            System.out.println("\n------------------------------");
            System.out.println("Project not completely converted. There were issues along the way.");
            System.out.println("------------------------------");
        }
    }

    /**
     * Copy project files from the source to the destination module. This assumes the destination module shell has already been created.
     *
     * @param sourceDir         Source project
     * @param destModuleRootDir Destination module.
     * @param sourceTestDir     Directory of the Test project.
     * @return True if all files are copied successfully, false if not.
     */
    public static boolean copyProjectFiles(final File sourceDir, final File destModuleRootDir, final File sourceTestDir) {
        final File destModuleMainDir = Utils.getDirectory(destModuleRootDir.getPath() + "/src/main", true);
        //Copy everything except for IDE files and Local files

        final File[] sourceFiles = sourceDir.listFiles(Utils.IDE_FILTER);
        System.out.println("Begin copying project files");
        boolean hasCopiedTestProject = false;

        try {
            for (final File file : sourceFiles) {
                final String srcName = file.getName();
                System.out.print("Copying " + file.getPath());
                final File destDir;
                if (srcName.equals("src")) {
                    //copy contents of src to src/main/java
                    destDir = new File(destModuleMainDir.getPath() + "/java");
                } else if (srcName.startsWith("lib")) {
                    //modify gradle file with all libs
                    destDir = new File(destModuleRootDir.getPath() + "\\libs");
                    addDependenciesToGradleFile(file, destModuleRootDir, Utils.DependencyType.compile);
                } else if (sourceTestDir != null && srcName.equals(sourceTestDir.getName())) {
                    copyTestProjectFiles(destModuleRootDir, sourceTestDir);
                    hasCopiedTestProject = true;
                    destDir = null;
                } else {
                    //any other file
                    destDir = new File(destModuleMainDir.getPath() + "\\" + srcName);
                }
                if (destDir != null) {
                    if (file.isDirectory()) {
                        FileUtils.copyDirectory(file, destDir, true);
                    } else {
                        FileUtils.copyFile(file, destDir, true);
                    }
                }
                if (destDir != null)
                    System.out.println(" to " + destDir.getPath());
                else {
                    System.out.println();
                }
            }

            //copy test project if it hasn't been copied.
            if (!hasCopiedTestProject && sourceTestDir != null) {
                copyTestProjectFiles(destModuleRootDir, sourceTestDir);
                hasCopiedTestProject = true;
            }
        } catch (IOException e) {
            System.out.println("\nERROR!! Could not copy file/directory.");
            e.printStackTrace();
            return false;
        }

        System.out.println("All files copied.");
        return true;
    }

    /**
     * Copy the files from a test project into it's destination module
     *
     * @param destModuleRootDir Destination module.
     * @param sourceTestDir     Source Test directory.
     * @return true if the operation is successful, false if not.
     */
    public static boolean copyTestProjectFiles(final File destModuleRootDir, final File sourceTestDir) throws IOException {
        System.out.print(" (Copying Test Project) ");
        //libs
        File file = Utils.getDirectory(sourceTestDir.getPath() + "/libs", false);
        if (file != null && file.isDirectory()) {
            FileUtils.copyDirectory(file, new File(destModuleRootDir.getPath() + "\\libs"), true);
            addDependenciesToGradleFile(file, destModuleRootDir, Utils.DependencyType.instrumentTestCompile);
        }

        //res
        file = Utils.getDirectory(sourceTestDir.getPath() + "/res", false);
        if (file != null && file.isDirectory()) {
            FileUtils.copyDirectory(file, new File(destModuleRootDir.getPath() + "\\src\\instrumentTest\\res"), true);
        }

        //src
        file = Utils.getDirectory(sourceTestDir.getPath() + "/src", false);
        if (file != null && file.isDirectory()) {
            FileUtils.copyDirectory(file, new File(destModuleRootDir.getPath() + "\\src\\instrumentTest\\java"), true);
        }
        return true;
    }

    /**
     * Add all libs from the directory to the module build.gradle file.
     *
     * @param libDir         Libs directory
     * @param moduleRootDir  Root directory of the module
     * @param dependencyType Type of dependency (compile, instrumentTestCompile)
     * @return true if the operation was successful, false if not.
     */
    public static boolean addDependenciesToGradleFile(final File libDir, final File moduleRootDir, final Utils.DependencyType dependencyType) {
        assert (libDir != null && libDir.isDirectory());
        assert (moduleRootDir != null && moduleRootDir.isDirectory());
        System.out.print(" (Adding dependencies to gradle file) ");

        final String prefix = "\t" + dependencyType + " files('" + libDir.getName() + "/";

        StringBuilder sb = new StringBuilder();

        //build string
        final File[] files = libDir.listFiles();
        if (files == null)
            return true;

        for (final File file : files) {
            final String fileName = file.getName();
            if (!file.isDirectory() && fileName.endsWith(".jar")) {
                sb.append(prefix + fileName + "')\n");
            }
        }

        final File buildGradle = new File(moduleRootDir.getPath() + "/build.gradle");
        final File buildGradleWithDependencies = new File(moduleRootDir.getPath() + "/build-dep.gradle");
        BufferedReader br = null;
        BufferedWriter bw = null;
//        System.out.println("\n------\nReading gradle file");
        try {
            String line;
            br = new BufferedReader(new FileReader(buildGradle));
            bw = new BufferedWriter(new FileWriter(buildGradleWithDependencies));
            while ((line = br.readLine()) != null) {
//                System.out.println(line);
                bw.write(line);
                bw.newLine();
                if (line.trim().equals(DEPENDENCY_PLACE_HOLDER)) {
//                    System.out.println(sb.toString());
                    bw.write(sb.toString());
                }
            }
            bw.flush();
//            System.out.println("Done reading gradle file\n------");
        } catch (FileNotFoundException e) {
            System.out.println("Warning!! Cannot add dependencies to module gradle file, the gradle file does not exist.");
            return false;
        } catch (IOException e) {
            System.out.println("Warning!! Cannot add dependencies to module gradle file, there were problems reading/writing to the file.");
            return false;
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //move new build file to old build file.
        try {
            FileUtils.forceDelete(buildGradle);
            FileUtils.moveFile(buildGradleWithDependencies, buildGradle);
        } catch (IOException e) {
            System.out.println("Could not save the gradle file with dependencies.");
            return false;
        }
        return true;
    }


    /**
     * Create an AndroidStudio shell project.
     *
     * @param dest          root directory.
     * @param moduleName    Name of the project
     * @param sourceTestDir Directory of the Test project.
     * @throws IOException Thrown if there is an error creating files or directories.
     */
    public static void createAndroidStudioShellProject(final File dest, final String moduleName, final File sourceTestDir) throws IOException {
        assert (dest != null);
        assert (moduleName != null && moduleName.length() > 0);
        System.out.println("\n==============================");
        System.out.println("Creating Android Shell Project");
        System.out.println("==============================");

        //copy gradle.wrapper
        //create directory
        File src = Utils.getDirectory(dest.getPath() + "/gradle/wrapper", true);
        if (!Utils.copyFileFromResourcesToFile("gradle_project_template/gradle-wrapper.jar", src.getPath() + "/gradle-wrapper.jar") ||
                !Utils.copyFileFromResourcesToFile("gradle_project_template/gradle-wrapper.properties", src.getPath() + "/gradle-wrapper.properties")) {
            System.out.println("Warning! Could not copy gradle wrapper.");
        }

        //copy build.gradle file
        Utils.copyFileFromResourcesToFile("gradle_project_template/build.gradle", dest.getPath() + "/build.gradle");
        //copy gradle wrapper executables
        Utils.copyFileFromResourcesToFile("gradle_project_template/gradlew", dest.getPath() + "/gradlew");
        Utils.copyFileFromResourcesToFile("gradle_project_template/gradlew.bat", dest.getPath() + "/gradlew.bat");

        //create settings.gradle file
        final String settingsFileName = dest.getPath() + "/settings.gradle";
        src = new File(settingsFileName);
        src.createNewFile();
        src.setWritable(true);
        FileWriter fw = new FileWriter(src.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("include ':" + moduleName + "'");
        bw.newLine();
        bw.close();

        //create module
        createAndroidStudioModule(dest, moduleName, sourceTestDir != null);
        System.out.println("Done Creating Project Shell");
        System.out.println("==============================");
    }

    /**
     * Create a module with the given parent directory and module name.
     *
     * @param parentProjectDir Parent project directory.
     * @param moduleName       Module name
     * @param createTestDir    True to create a test directory, false to not.
     * @throws IOException
     */
    public static void createAndroidStudioModule(final File parentProjectDir, final String moduleName, final boolean createTestDir) throws IOException {
        assert (parentProjectDir != null && parentProjectDir.isDirectory());
        assert (moduleName != null);
        System.out.println("=============");
        System.out.println("Creating Module (" + moduleName + ") Shell");
        System.out.println("=============");
        // create module
        final String modulePath = parentProjectDir.getPath() + "/" + moduleName;
        final File moduleDir = new File(modulePath);
        try {
            FileUtils.forceMkdir(moduleDir);
        } catch (IOException e) {
            System.out.println("Could not create module: " + modulePath);
            throw e;
        }

        //copy build.gradle
        Utils.copyFileFromResourcesToFile("gradle_project_template/module/build.gradle", moduleDir.getPath() + "/build.gradle");

        //create src/main/java
        Utils.getDirectory(moduleDir.getPath() + "/src/main/java", true);
        Utils.getDirectory(moduleDir.getPath() + "/src/main/res", true);
        //create instrument test folder if there are tests.
        if (createTestDir)
            Utils.getDirectory(moduleDir.getPath() + "/src/instrumentTest/java", true);

        System.out.println("Done Creating Module (" + moduleName + ") Shell");
        System.out.println("=============");
    }
}

