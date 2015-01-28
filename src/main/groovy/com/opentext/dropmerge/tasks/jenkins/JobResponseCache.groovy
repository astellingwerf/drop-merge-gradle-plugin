package com.opentext.dropmerge.tasks.jenkins

import com.opentext.dropmerge.dsl.JenkinsJob as JobInDsl
import com.opentext.dropmerge.dsl.JsonDataType
import com.opentext.dropmerge.jenkins.*
import org.gradle.api.logging.Logger

import static com.opentext.dropmerge.jenkins.JenkinsJob.LAST_COMPLETED_BUILD

class JobResponseCache implements Serializable {

    private static final long serialVersionUID = 42L;

    String jobName
    String serverUrl
    Set<JsonDataType> types
    Map<String, String> matrixAxes

    Date dateLastCompletedBuild

    JobResponseCache() {}

    JobResponseCache(JobInDsl job) {
        this.jobName = job.jobName
        this.serverUrl = job.server.url
        this.types = job.dataTypes
        this.matrixAxes = job.matrixAxes

        this.dateLastCompletedBuild = Jenkins.getInstance(serverUrl).withJob(jobName, matrixAxes, new ResponseReader() {
            @Override
            String getText(String url, Logger logger) {
                new URL(url).text
            }
        }).getBuildTimestamp(LAST_COMPLETED_BUILD)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        JobResponseCache that = (JobResponseCache) o

        if (dateLastCompletedBuild != that.dateLastCompletedBuild) return false
        if (jobName != that.jobName) return false
        if (matrixAxes != that.matrixAxes) return false
        if (serverUrl != that.serverUrl) return false
        if (types != that.types) return false

        return true
    }

    int hashCode() {
        int result
        result = jobName.hashCode()
        result = 31 * result + serverUrl.hashCode()
        result = 31 * result + types.hashCode()
        result = 31 * result + (matrixAxes != null ? matrixAxes.hashCode() : 0)
        result = 31 * result + (dateLastCompletedBuild != null ? dateLastCompletedBuild.hashCode() : 0)
        return result
    }

    public void cache(ResponseReader responseReader) {
        JenkinsJob job = Jenkins.getInstance(serverUrl).withJob(jobName, matrixAxes, responseReader)
        if (types.contains(JsonDataType.Tests)) {
            (TestCount.values() - TestCount.Total).each(job.&getTestFigure)
            job.color
            job.getBuildTimestamp(LAST_COMPLETED_BUILD)
            job.testReport
        }
        if (types.contains(JsonDataType.PMD)) {
            WarningLevel.values().each(job.&getPMDFigure)
            job.PMDReport
        }
        if (types.contains(JsonDataType.MBV)) {
            WarningLevel.values().each(job.&getMBFigure)
            job.MBVReport
        }
        if (types.contains(JsonDataType.CompilerWarnings)) {
            job.compilerWarningFigure
            job.compilerWarningsReport
        }
        if (types.contains(JsonDataType.Success)) {
            job.lastBuildResult
        }
        if (types.contains(JsonDataType.Color)) {
            job.color
        }
    }
}
