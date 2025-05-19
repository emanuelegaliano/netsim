package com.netsim.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Test;

public class LoggerTest {
    @Test
    public void testLog() throws IOException {
        Logger logger = Logger.getInstance();
        String testMsg = "DIRECT_LOG_TEST";

        logger.log(testMsg);
        Path logPath = Paths.get(logger.fileName);
        List<String> lines = Files.readAllLines(logPath);

        assertEquals("Message written must match", lines.getLast(), testMsg);
    }

    @After
    public void resetSingleton() {
        Logger.reset();
    }
}
