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

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";

    private Logger() {
        Properties props = new Properties();
        try {
            InputStream input = Logger.class
                                      .getClassLoader()
                                      .getResourceAsStream("application.properties");

            if(input == null) {
                System.err.println("Unable to load application properties");
            } else {
                props.load(input);
            }                 
        } catch(IOException e) {
            System.err.println("Unable to load application properties");
        }

        this.fileName = props.getProperty("LOG_FILE", "default.log");
        this.logFile  = Paths.get(this.fileName);

        this.cleanFile();
    }

    /**
     * Delete all the content in the
     * file in application.properties
     */
    private void cleanFile() {
        try {
            Files.write(this.logFile, new byte[0]);
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
     * This method returns the istance of the logger.
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
     * This method should be called when information need to be logged
     * @param msg The info message to be logged
     */
    public void info(String msg) {
        String logMsg = "LOGGER INFO: " + msg;
        this.log(logMsg);
        System.out.println(GREEN + logMsg + RESET);
    }

    /**
     * This method should be called when errors need to be logged
     * @param err The error message to be logged
     */
    public void error(String err) {
        String logMsg = "LOGGER ERROR: " + err;
        this.log(logMsg);
        System.err.println(RED + logMsg + RESET);
    }

    /**
     * This method should be called when debug message need to be logged
     * @param msg The debug message to be logged
     */
    public void debug(String msg) {
        String logMsg = "LOGGER DEBUG: " + msg;
        this.log(logMsg);
        System.out.println(BLUE + logMsg + RESET);
    }
}