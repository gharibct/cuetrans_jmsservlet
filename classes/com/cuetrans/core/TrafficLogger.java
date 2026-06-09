package com.cuetrans.core;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrafficLogger {
    private static final Logger LOGGER = Logger.getLogger("TrafficLogger");

    static {
        try {
            // Logs will be written to ServletTraffic.log (size 5MB, 10 rotating files)
            FileHandler fileHandler = new FileHandler("ServletTraffic%g.log", 5242880, 10, true);
            fileHandler.setFormatter(new SingleLineLogFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false); // Only log to the file, not the console
        } catch (IOException e) {
            System.err.println("Could not initialize TrafficLogger: " + e.getMessage());
        }
    }

    public static void log(String message) {
        LOGGER.log(Level.INFO, message);
    }
}
