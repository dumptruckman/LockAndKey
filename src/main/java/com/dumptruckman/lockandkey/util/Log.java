/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.util;

import org.jetbrains.annotations.NotNull;
import pluginbase.logging.PluginLogger;

import java.util.logging.Level;

public class Log {

    private Log() {
        throw new AssertionError();
    }

    @NotNull
    private static volatile PluginLogger pluginLogger = null;

    public static void init(@NotNull PluginLogger logger) {
        pluginLogger = logger;
    }

    public static void shutdown() {
        pluginLogger = null;
    }

    /**
     * Returns the single anonymous PluginLogger instance for this static logging class.
     *
     * @return the anonymous PluginLogger used for static logging by this class.
     */
    @NotNull
    public static PluginLogger getLogger() {
        return pluginLogger;
    }

    /**
     * Sets the debug logging level of this plugin.
     * <p/>
     * Debug messages will print to the console and to a debug log file when enabled.
     * <p/>
     * debugLevel:
     * <br/>0 - turns off debug logging, disabling the debug logger, closing any open file hooks.
     * <br/>1 - enables debug logging of {@link java.util.logging.Level#FINE} or lower messages.
     * <br/>2 - enables debug logging of {@link java.util.logging.Level#FINER} or lower messages.
     * <br/>3 - enables debug logging of {@link java.util.logging.Level#FINEST} or lower messages.
     *
     * @param debugLevel 0 = off, 1-3 = debug level
     */
    public static void setDebugLevel(final int debugLevel) {
        pluginLogger.setDebugLevel(debugLevel);
    }

    /**
     * Custom log method that always logs to a single static logger.
     * <p/>
     * Applies String.format() to the message if it is a non-debug level logging and to debug level logging IF debug
     * logging is enabled.
     * <br/>
     * Optionally appends version to prefix.
     *
     * @param level       One of the message level identifiers, e.g. SEVERE.
     * @param message     The string message.
     * @param args        Arguments for the String.format() that is applied to the message.
     */
    public static void log(@NotNull final Level level, @NotNull final String message, @NotNull final Object... args) {
        pluginLogger.log(level, message, args);
    }

    /**
     * Fine debug level logging.  Use for infrequent messages.
     *
     * @param message Message to log.
     * @param args    Arguments for the String.format() that is applied to the message.
     */
    public static void fine(@NotNull final String message, @NotNull final Object...args) {
        pluginLogger.fine(message, args);
    }

    /**
     * Finer debug level logging.  Use for somewhat frequent messages.
     *
     * @param message Message to log.
     * @param args    Arguments for the String.format() that is applied to the message.
     */
    public static void finer(@NotNull final String message, @NotNull final Object...args) {
        pluginLogger.finer(message, args);
    }

    /**
     * Finest debug level logging.  Use for extremely frequent messages.
     *
     * @param message Message to log.
     * @param args    Arguments for the String.format() that is applied to the message.
     */
    public static void finest(@NotNull final String message, @NotNull final Object...args) {
        pluginLogger.finest(message, args);
    }

    /**
     * Config level logging.  Use for messages that should be INFO level but have the option to be disabled
     * via debug level -1.
     *
     * @param message Message to log.
     * @param args    Arguments for the String.format() that is applied to the message.
     */
    public static void config(@NotNull final String message, @NotNull final Object...args) {
        pluginLogger.config(message, args);
    }

    /**
     * Info level logging.
     *
     * @param message Message to log.
     * @param args    Arguments for the String.format() that is applied to the message.
     */
    public static void info(@NotNull final String message, @NotNull final Object...args) {
        pluginLogger.info(message, args);
    }

    /**
     * Warning level logging.
     *
     * @param message Message to log.
     * @param args    Arguments for the String.format() that is applied to the message.
     */
    public static void warning(@NotNull final String message, @NotNull final Object...args) {
        pluginLogger.warning(message, args);
    }

    /**
     * Severe level logging.
     *
     * @param message Message to log.
     * @param args    Arguments for the String.format() that is applied to the message.
     */
    public static void severe(@NotNull final String message, @NotNull final Object...args) {
        pluginLogger.severe(message, args);
    }
}
