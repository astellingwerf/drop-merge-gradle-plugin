package com.opentext.dropmerge.jenkins

import groovy.transform.Memoized
import org.gradle.api.logging.Logger

class ResponseCacheReader extends ResponseReader {

    @Memoized
    String getText(String url, Logger logger) {
        String cacheKey = getCacheKey(url)
        File f = new File("build/jenkins/cache/${cacheKey}.txt")

        if (f.exists()) {
            logger?.debug('Read {} from cache.', url)
            return f.text
        } else {
            logger?.warn('Reading {} directly.', url)
            new URL(url).text
        }
    }

}
