package com.opentext.dropmerge.tasks.jenkins

import com.opentext.dropmerge.dsl.JenkinsJob
import com.opentext.dropmerge.jenkins.ResponseReader
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class UpdateResponseCache extends DefaultTask {

    JenkinsJob job

    @Input
    public JobResponseCache getJobResponseCache() {
        new JobResponseCache(job)
    }

    @OutputDirectory
    public File getCacheDir() {
        new File(project.buildDir, 'jenkins/cache/')
    }

    @TaskAction
    public void updateCache() {
        ResponseReader reader = new ResponseReader() {
            @Override
            String getText(String url, Logger unused) {
                logger.info('Reading {} directly into cache.', url)
                String src = new URL(url).text
                new File(cacheDir, "${getCacheKey(url)}.txt").text = src
                return src
            }
        }
        jobResponseCache.cache(reader)
    }

    static String getTaskName(JenkinsJob job) { "updateResponseCache-${job.name}" }

    static Collection<String> getTaskNames(Collection<JenkinsJob> jobs) { jobs.collect { getTaskName(it) } }

}
