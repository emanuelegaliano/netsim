// utils/Logger.java

package com.netsim.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class Logger {
    private static Logger instance = null;
    private final Path logFile;
    public final String fileName;
    private final boolean logOnConsole;

    public static final String RESET = "\u001B[0m";
    public static final String RED   = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW= "\u001B[33m";
    public static final String BLUE  = "\u001B[34m";

    private Logger() {
        Properties props = new Properties();
        boolean consoleFlag = false;

        try (InputStream input = Logger.class
                                    .getClassLoader()
                                    .getResourceAsStream("application.properties")) {

            if (input != null) {
                props.load(input);
                consoleFlag = Boolean.parseBoolean(
                    props.getProperty("LOG_ON_CONSOLE", "false").trim()
                );
            } else {
                System.err.println("Unable to load application properties, defaulting LOG_ON_CONSOLE=false");
            }
        } catch(IOException e) {
            System.err.println("Unable to load application properties: " + e.getMessage());
        }

        this.logOnConsole = consoleFlag;
        this.fileName     = props.getProperty("LOG_FILE", "default.log");
        this.logFile      = Paths.get(this.fileName);

        this.cleanFile();
    }

    /**
     * Deletes (or creates) the log file and its parent directories.
     */
    private void cleanFile() {
        try {
            Path parent = logFile.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            // create or truncate the file
            Files.write(
                logFile,
                new byte[0],
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            System.err.println("Failed to clear log file " + this.fileName + ": " + e.getMessage());
        }
    }

    /**
     * Reset the instance of Logger
     */
    public static void reset() {
        instance = null;
    }

    /**
     * Log the message only in the log file
     * @param msg message to log
     */
    public void log(String msg) {
        try {
            Files.writeString(
                logFile,
                msg + "\n",
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch(IOException e) {
            System.out.println(YELLOW + "Unable to open file " + this.fileName + RESET);
        }
    }

    /**
     * This method returns the instance of the logger.
     * If the instance is null (no call yet) then instantiate it
     * and returns the instance
     * 
     * @return Logger instance
     */
    public static Logger getInstance() {
        if(instance == null) 
            instance = new Logger();
        return instance;
    }

    /**
     * Info‐level log. Always written to file; printed to console only
     * if LOG_ON_CONSOLE=true.
     */
    public void info(String msg) {
        String logMsg = "LOGGER INFO: " + msg;
        this.log(logMsg);
        if (logOnConsole) {
            System.out.println(GREEN + logMsg + RESET);
        }
    }

    /**
     * Error‐level log. Always written to file; printed to stderr only
     * if LOG_ON_CONSOLE=true.
     */
    public void error(String err) {
        String logMsg = "LOGGER ERROR: " + err;
        this.log(logMsg);
        if (logOnConsole) {
            System.err.println(RED + logMsg + RESET);
        }
    }

    /**
     * Debug‐level log. Always written to file; printed to console only
     * if LOG_ON_CONSOLE=true.
     */
    public void debug(String msg) {
        String logMsg = "LOGGER DEBUG: " + msg;
        this.log(logMsg);
        if (logOnConsole) {
            System.out.println(BLUE + logMsg + RESET);
        }
    }
}