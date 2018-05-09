package io.sentry.config;

import io.sentry.dsn.Dsn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Handle lookup of configuration keys by trying JNDI, System Environment, and Java System Properties.
 */
public final class Lookup {
    private static final Logger logger = LoggerFactory.getLogger(Lookup.class);

    /**
     * The filename of the Sentry configuration file.
     */
    private static final String CONFIG_FILE_NAME = "sentry.properties";
    /**
     * Properties loaded from the Sentry configuration file, or null if no file was
     * found or it failed to parse.
     */
    private static Properties configProps;

    static {
        String filePath = getConfigFilePath();
        try {
            InputStream input = getInputStream(filePath);

            if (input != null) {
                configProps = new Properties();
                configProps.load(input);
            } else {
                logger.debug("Sentry configuration file not found in filesystem or classpath: '{}'.", filePath);
            }
        } catch (Exception e) {
            logger.error("Error loading Sentry configuration file '{}': ", filePath, e);
        }
    }

    /**
     * Hidden constructor for static utility class.
     */
    private Lookup() {

    }

    private static String getConfigFilePath() {
        String filePath = System.getProperty("sentry.properties.file");

        if (filePath == null) {
            filePath = System.getenv("SENTRY_PROPERTIES_FILE");
        }

        if (filePath == null) {
            filePath = CONFIG_FILE_NAME;
        }

        return filePath;
    }

    private static InputStream getInputStream(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        if (file.isFile() && file.canRead()) {
            return new FileInputStream(file);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(filePath);
    }

    /**
     * Attempt to lookup a configuration key, without checking any DSN options.
     *
     * @param key name of configuration key, e.g. "dsn"
     * @return value of configuration key, if found, otherwise null
     */
    public static String lookup(String key) {
        return lookup(key, null);
    }

    /**
     * Attempt to lookup a configuration key using the following order:
     *
     * 1. Java System Properties
     * 2. System Environment Variables
     * 3. DSN options, if a non-null DSN is provided
     * 4. Sentry properties file found in resources
     *
     * @param key name of configuration key, e.g. "dsn"
     * @param dsn an optional DSN to retrieve options from
     * @return value of configuration key, if found, otherwise null
     */
    public static String lookup(String key, Dsn dsn) {
        // Try to obtain from a Java System Property
        String value = System.getProperty("sentry." + key.toLowerCase());
        if (value != null) {
            logger.debug("Found {}={} in Java System Properties.", key, value);
        }

        // Try to obtain from a System Environment Variable
        if (value == null) {
            value = System.getenv("SENTRY_" + key.replace(".", "_").toUpperCase());
            if (value != null) {
                logger.debug("Found {}={} in System Environment Variables.", key, value);
            }
        }

        // Try to obtain from the provided DSN, if set
        if (value == null && dsn != null) {
            value = dsn.getOptions().get(key);
            if (value != null) {
                logger.debug("Found {}={} in DSN.", key, value);
            }
        }

        // Try to obtain from config file
        if (value == null && configProps != null) {
            value = configProps.getProperty(key);
            if (value != null) {
                logger.debug("Found {}={} in {}.", key, value, CONFIG_FILE_NAME);
            }
        }

        if (value != null) {
            return value.trim();
        } else {
            return null;
        }
    }

}
