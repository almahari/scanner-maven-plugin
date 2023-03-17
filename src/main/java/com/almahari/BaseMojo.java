package com.almahari;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class BaseMojo extends AbstractMojo {

    @Parameter(property = "verbose")
    int verbose = 1;

    protected void logRed(String text) {
        logColor("1", text);
    }

    protected void logGreen(String text) {
        logColor("2", text);
    }

    protected void logYellow(String text) {
        logColor("3", text);
    }

    protected void logBlue(String text) {
        logColor("4", text);
    }

    protected void logDashes(String text) {
        logColor("6", "=========================" + text + "=========================");
    }

    protected void log(String text) {
        getLog().info(text);
    }

    protected void error(String text, Throwable throwable) {
        getLog().error(text, throwable);
    }

    protected void logV(String text) {
        if (verbose >= 1) {
            log(text);
        }
    }

    protected void logVV(String text) {
        if (verbose >= 2) {
            logYellow(text);
        }
    }

    private void logColor(String color, String text) {
        log("\033[0;3" + color + "m" + text + "\033[0m");
    }
}
