package com.opentext.dropmerge.jenkins

import org.gradle.api.logging.Logger

abstract class ResponseReader {
    abstract String getText(String url, Logger logger)

    String getCacheKey(String url) {
        return url.hashCode()
    }
}
