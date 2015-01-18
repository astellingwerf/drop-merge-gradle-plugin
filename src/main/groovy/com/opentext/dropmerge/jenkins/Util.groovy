package com.opentext.dropmerge.jenkins;

public class Util {
    public static Closure getJenkinsUrl(JenkinsJob job, String build = null, String linkText = null) {
        return {
            a(href: job.getBuildUrl(build), linkText ?: job.toString())
        }
    }

    public static Closure getJenkinsUrlWithStatus(JenkinsJob job, String build = null, String linkText = null) {
        return {
            span(class: "jenkinsJobStatus jenkinsJobStatus_${job.color}", getJenkinsUrl(job, build, linkText))
        }
    }
}
