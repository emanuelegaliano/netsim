package com.netsim.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Singleton logger utility that writes to a file (and optionally to console),
 * supporting INFO, ERROR, and DEBUG levels.
 */
public class Logger {
    private static final String RESET       = "\u001B[0m";
    private static final String RED         = "\u001B[31m";
    private static final String GREEN       = "\u001B[32m";
    private static final String YELLOW      = "\u001B[33m";
    private static final String BLUE        = "\u001B[34m";
    private static final Logger instance    = createInstance();

    private final Path    logFile;
    private final String  fileName;
    private final boolean logOnConsole;
    private final boolean debugLevelOn;
    private final boolean errorLevelOn;
    private final boolean infoLevelOn;

    private Logger(Path logFile,
                   String fileName,
                   boolean logOnConsole,
                   boolean debugLevelOn,
                   boolean errorLevelOn,
                   boolean infoLevelOn) {
        this.logFile       = logFile;
        this.fileName      = fileName;
        this.logOnConsole  = logOnConsole;
        this.debugLevelOn  = debugLevelOn;
        this.errorLevelOn  = errorLevelOn;
        this.infoLevelOn   = infoLevelOn;
        cleanFile();
    }

    private static Logger createInstance() {
        Properties props = new Properties();
        boolean   consoleFlag = false;
        String    fname       = "default.log";

        try (InputStream in = Logger.class.getClassLoader()
                                          .getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
                consoleFlag = Boolean.parseBoolean(props.getProperty("LOG_ON_CONSOLE", "false").trim());
                fname       = props.getProperty("LOG_FILE", fname).trim();
            } else {
                System.err.println("Unable to load application properties, defaulting LOG_ON_CONSOLE=false");
            }
        } catch (IOException e) {
            System.err.println("Unable to load application properties: " + e.getMessage());
        }

        Path logPath = Paths.get(fname);
        return new Logger(logPath,
                          fname,
                          consoleFlag,
                          true,   // debugLevelOn
                          true,   // errorLevelOn
                          true);  // infoLevelOn
    }

    /**
     * Clears (or creates) the log file and its parent directories.
     */
    private void cleanFile() {
        try {
            Path parent = this.logFile.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.write(this.logFile,
                        new byte[0],
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to clear log file " + this.fileName + ": " + e.getMessage());
        }
    }

    /**
     * Resets the singleton Logger instance (for testing).
     */
    public static void reset() {
        // No-op: singleton cannot be reset in this implementation
    }

    /**
     * @return the singleton Logger instance
     */
    public static Logger getInstance() {
        return instance;
    }

    /**
     * Logs a raw message to the log file.
     *
     * @param msg the message to append (non-null)
     */
    public void log(String msg) {
        try {
            Files.writeString(this.logFile,
                              msg + "\n",
                              StandardOpenOption.CREATE,
                              StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println(YELLOW + "Unable to open file " + this.fileName + RESET);
        }
    }

    /**
     * Logs an INFO-level message.
     *
     * @param msg the message to log (non-null)
     */
    public void info(String msg) {
        if (!this.infoLevelOn) {
            return;
        }
        String logMsg = String.format("LOGGER %5s:\t%s", "INFO", msg);
        log(logMsg);
        if (this.logOnConsole) {
            System.out.println(GREEN + logMsg + RESET);
        }
    }

    /**
     * Logs an ERROR-level message.
     *
     * @param err the error message to log (non-null)
     */
    public void error(String err) {
        if (!this.errorLevelOn) {
            return;
        }
        String logMsg = String.format("LOGGER %5s:\t%s", "ERROR", err);
        log(logMsg);
        if (this.logOnConsole) {
            System.err.println(RED + logMsg + RESET);
        }
    }

    /**
     * Logs a DEBUG-level message.
     *
     * @param msg the debug message to log (non-null)
     */
    public void debug(String msg) {
        if (!this.debugLevelOn) {
            return;
        }
        String logMsg = String.format("LOGGER %5s:\t%s", "DEBUG", msg);
        log(logMsg);
        if (this.logOnConsole) {
            System.out.println(BLUE + logMsg + RESET);
        }
    }

    /** @return logging filenam */
    public String getFilename() {
        return this.fileName;
    }
}