package com.opentext.dropmerge

import com.opentext.dropmerge.dsl.*
import com.opentext.dropmerge.tasks.ListWikiFields
import com.opentext.dropmerge.tasks.PrintConfiguration
import com.opentext.dropmerge.tasks.UpdateWiki
import com.opentext.dropmerge.tasks.jenkins.UpdateResponseCache
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class DropMergeWikiPlugin implements Plugin<Project> {

    public static final String DROP_MERGE_GROUP = 'Drop merge'
    private UpdateWiki updateWikiTask
    private Task updateResponseCache

    @Override
    void apply(Project project) {
        applyTasks(project)
        applyExtensions(project)
    }

    void applyTasks(Project project) {
        project.task('printConfiguration',
                type: PrintConfiguration,
                group: DROP_MERGE_GROUP,
                description: 'Print the drop merge configuration.')
        project.task('listWikiFields',
                type: ListWikiFields,
                group: DROP_MERGE_GROUP,
                description: 'Print what field are and aren\'t covered.')
        updateWikiTask = project.task('updateWiki',
                type: UpdateWiki,
                group: DROP_MERGE_GROUP,
                description: 'Persist the gathered data on the wiki page.')
        updateResponseCache = project.task('updateResponseCache',
                group: DROP_MERGE_GROUP,
                description: 'Update all response caches.')
    }

    void applyExtensions(Project project) {
        def servers = project.container(JenkinsServer)
        def jobs = project.container(JenkinsJob)
        def regressionTests = project.container(RegressionTest)
        def qualityQuestions = project.container(QualityAndProcessQuestion)

        jobs.all { JenkinsJob job ->
            updateResponseCache.dependsOn project.task(UpdateResponseCache.getTaskName(job),
                    type: UpdateResponseCache,
                    group: DROP_MERGE_GROUP,
                    description: "Update the response cache for Jenkins Job: ${job.name}.").configure {
                delegate.job = job
            }
        }

        project.extensions.create('dropMerge', DropMergeConfiguration, updateWikiTask, servers, jobs, regressionTests, qualityQuestions)
    }
}
