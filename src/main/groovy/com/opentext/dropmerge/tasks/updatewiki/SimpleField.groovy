package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.DropMergeConfiguration
import com.opentext.dropmerge.dsl.JenkinsJob as JobInDsl
import com.opentext.dropmerge.jenkins.Jenkins
import com.opentext.dropmerge.jenkins.JenkinsJob
import com.opentext.dropmerge.wiki.CordysWiki
import com.opentext.dropmerge.wiki.FormField
import org.gradle.api.DefaultTask

public class SimpleField extends DefaultTask {
    String fieldName
    Map<String, String> results

    DropMergeConfiguration getConfig() {
        return project.extensions.dropMerge
    }

    FormField getFormField() {
        CordysWiki.getInstance(config.wiki.userName, config.wiki.password).with {
            getDropMergeFields(config.wiki.pageId)[fieldName]
        }
    }

    void setResult(String fieldNameAppendix = '', String value) {
        results[fieldName + fieldNameAppendix] = value
    }

    String getResult(String fieldNameAppendix = '') {
        results[fieldName + fieldNameAppendix]
    }

    void setSelectedOption(String option) {
        result = formField.hasOptions() ? CordysWiki.selectOption(formField.rawItem, option) : option
    }

    Collection<String> getFieldNames() {
        [fieldName]
    }

	JenkinsJob getJenkinsJob(JobInDsl job) {
		JenkinsJob jenkinsJob = Jenkins.getInstance(job.server.url).withJob(job.jobName, job.matrixAxes)
		jenkinsJob.logger = logger;
		return jenkinsJob
	}
}
