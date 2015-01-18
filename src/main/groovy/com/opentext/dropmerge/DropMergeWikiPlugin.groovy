package com.opentext.dropmerge

import com.opentext.dropmerge.dsl.*
import com.opentext.dropmerge.tasks.*
import org.gradle.api.*

class DropMergeWikiPlugin implements Plugin<Project> {

    public static final String DROP_MERGE_GROUP = 'Drop merge'

    @Override
    void apply(Project project) {
        applyTasks(project)
        applyExtensions(project)
    }

    void applyTasks(project) {
        project.task('printConfiguration', type: PrintConfiguration, group: DROP_MERGE_GROUP, description: 'Print the drop merge configuration.')
        project.task('updateWiki', type: UpdateWiki, group: DROP_MERGE_GROUP, description: 'Persist the gathered data on the wiki page.')
    }

    void applyExtensions(project) {
        def servers = project.container(JenkinsServer)
        def jobs = project.container(JenkinsJob)
        def regressionTests = project.container(RegressionTest)
        def qualityQuestions = project.container(QualityAndProcessQuestion)

        project.extensions.create('dropMerge', DropMergeConfiguration, servers, jobs, regressionTests, qualityQuestions)
    }
}
