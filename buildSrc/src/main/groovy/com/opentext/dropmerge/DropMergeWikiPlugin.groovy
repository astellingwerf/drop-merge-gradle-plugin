package com.opentext.dropmerge

import com.opentext.dropmerge.dsl.*
import org.gradle.api.*;

class DropMergeWikiPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        applyTasks(project)
        applyConventions(project)
    }

    void applyTasks(project) {
        project.task('printConfiguration') << {
            println project.convention.plugins.dropMerge
        }
    }

    void applyConventions(project) {
        def servers = project.container(JenkinsServer)
        def jobs = project.container(JenkinsJob)
        def regressionTests = project.container(RegressionTest)

        def configuration = new DropMergeConfiguration(servers, jobs, regressionTests)
        project.convention.plugins.dropMerge = new DropMergeConfigurationConvention(configuration)
    }
}
