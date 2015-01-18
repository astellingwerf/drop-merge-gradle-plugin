package com.opentext.dropmerge

import com.opentext.dropmerge.dsl.*
import com.opentext.dropmerge.tasks.ListWikiFields
import com.opentext.dropmerge.tasks.PrintConfiguration
import com.opentext.dropmerge.tasks.UpdateWiki
import org.gradle.api.Plugin
import org.gradle.api.Project

class DropMergeWikiPlugin implements Plugin<Project> {

    public static final String DROP_MERGE_GROUP = 'Drop merge'
    private UpdateWiki updateWikiTask

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
    }

    void applyExtensions(Project project) {
        def servers = project.container(JenkinsServer)
        def jobs = project.container(JenkinsJob)
        def regressionTests = project.container(RegressionTest)
        def qualityQuestions = project.container(QualityAndProcessQuestion)

        project.extensions.create('dropMerge', DropMergeConfiguration, updateWikiTask, servers, jobs, regressionTests, qualityQuestions)
    }
}
