package com.opentext.dropmerge.tasks

import com.opentext.dropmerge.crucible.Crucible
import com.opentext.dropmerge.dsl.DropMergeConfiguration
import com.opentext.dropmerge.jenkins.Jenkins
import com.opentext.dropmerge.jenkins.JenkinsJob
import com.opentext.dropmerge.tasks.updatewiki.*
import com.opentext.dropmerge.wiki.CordysWiki
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

import static com.opentext.dropmerge.DropMergeWikiPlugin.DROP_MERGE_GROUP
import static com.opentext.dropmerge.crucible.Crucible.getCrucibleAuthToken
import static com.opentext.dropmerge.crucible.Crucible.getOpenReviewCount
import static com.opentext.dropmerge.jenkins.TestCount.Fail
import static com.opentext.dropmerge.jenkins.TestCount.Pass
import static com.opentext.dropmerge.jenkins.WarningLevel.High
import static com.opentext.dropmerge.jenkins.WarningLevel.Normal
import static com.opentext.dropmerge.wiki.WikiTableBuilder.withHtml

class UpdateWiki extends DefaultTask {

    static final SUB_TASK_PREFIX = 'fillDropMergeField'
    static Task updateAllTask
    Map<String, String> resultingData = [:]

    DropMergeConfiguration getConfiguration() {
        return project.extensions.dropMerge
    }

    public UpdateWiki() {
        super()
        if (!updateAllTask) {
            updateAllTask = project.task('updateWiki-all', group: DROP_MERGE_GROUP, description:"Void task to invoke all '$SUB_TASK_PREFIX*' tasks.").dependsOn this
        }

        // 0
        registerDependencyTaskForField('TeamLink') { selectedOption = config.team.name }
        // TODO: drop merge date
        // TODO: drop merge revision
        // TODO: Functional description

        // 1
        registerDependencyTaskForField('ReviewsDone', SimpleFieldWithComment, null) {
            if (!config.crucible.userName) throw new IllegalArgumentException('Crucible username not provided or empty')
            if (!config.crucible.password) throw new IllegalArgumentException('Crucible password not provided or empty')
            if (!config.crucible.projectKey) throw new IllegalArgumentException('Crucible project not provided or empty')

            String crucibleAuthToken = getCrucibleAuthToken(config.crucible.userName, config.crucible.password)
            int openReviewCount = getOpenReviewCount(config.crucible.projectKey, crucibleAuthToken)

            selectedOption = openReviewCount == 0 ? 'Yes' : 'No'
            comment = withHtml { html ->
                html.a(href: Crucible.getBrowseReviewsURL(config.crucible.projectKey), openReviewCount == 0 ? 'All reviews closed' : "$openReviewCount open review(s)")
            }
        }

        // 2 and 3
        ['PerformanceDegradation',
         'MemoryLeaksIntroduced'].each this.&registerQualityQuestion

        // 4
        registerDependencyTaskForField('IntegrationTestsPass', JenkinsJobStatus, { set { config.integrationTests } })

        // 5, 6, and 7
        registerDependencyTaskForField('SuccesfulTests', ComparableTestCount, { set Pass })
        registerDependencyTaskForField('FailedTests', ComparableTestCount, { set Fail })
        registerDependencyTaskForField('SuccessfulRegressionTestsComment', SuccessfulRegressionTestsComment)
        registerDependencyTaskForField('TotalRegressionTestsComment', TotalRegressionTestsComment)

        // 8 through 13
        ['NewManualTestCases',
         'RegressionTestsPassWithPayloadValidation',
         'CompliantWithHorizontalComponentRequirements',
         'DocumentationReviewed',
         'TranslatableMessages',
         'DocumentedAlerts'].each this.&registerQualityQuestion

        // 14
        registerDependencyTaskForField('UpgradeTested', JenkinsJobStatus, { set { config.upgrade } })

        // 15, and 16
        ['MigrationAspectsHandled',
         'BackwardCompatibilityIssues'].each this.&registerQualityQuestion

        // 17, 18, 20, and 21
        [High: High, Medium: Normal].each { label, level ->
            registerDependencyTaskForField('MBViolations' + label, MBVCount, { set level })
            registerDependencyTaskForField('PMDViolations' + label, PMDCount, { set level })
        }

        // 19
        registerDependencyTaskForField('CompilerWarnings', CWCount)

        // 22 through 28
        ['SecurityIssuesIntroduced',
         'BuildAndInstallerChangesAddressed',
         'DefectFixesRetestedByOtherPerson',
         'UserStoriesAcceptedByPM',
         'UsabilityAcceptedByPM',
         'MultiplatformValidationDone',
         'ForwardPortingCompleted'].each this.&registerQualityQuestion

        // TODO: Final verdict
    }

    Task registerDependencyTaskForField(String field, Class<? extends Task> type, Closure configure = null, Closure action = null) {
        def t = project.task("$SUB_TASK_PREFIX$field",
                group: DROP_MERGE_GROUP,
                type: type,
                description: "Calculate the data for the field \'$field\'.")
                .configure({ fieldName = field; results = resultingData })
                .configure(configure)
        if (action)
            t << action
        t.finalizedBy this
        updateAllTask.dependsOn t
        return t
    }

    Task registerDependencyTaskForField(String field, Closure action) {
        registerDependencyTaskForField(field, SimpleField, null, action)
    }

    Task registerQualityQuestion(String field) {
        registerDependencyTaskForField(field, SimpleFieldWithComment, null, {
            def input = config.qualityAndProcessQuestions.findByName(field)
            if (!input) {
                didWork = false
                return
            }
            if(input.answer) selectedOption = input.answer
            if(input.comment) comment = input.comment
        })
    }

    @TaskAction
    public void updateWiki() {
        if (!configuration.wiki.userName) throw new IllegalArgumentException('wiki username not provided or empty')
        if (!configuration.wiki.password) throw new IllegalArgumentException('wiki password not provided or empty')
        if (!configuration.wiki.pageId) throw new IllegalArgumentException('wiki page id not provided or empty')

        if (!anyDependsOnTasksDidWork || !allDependsOnTasksWereOK) {
            didWork = false
            return
        }

        resultingData.collectEntries { k, v -> [(k): (!v ? 'null' : v.substring(0, Math.min(v.length(), 100)) + (v.length() > 100 ? '...' : ''))] }.each { k, v ->
            logger.info "${k.padLeft(resultingData.keySet()*.length().max())}: $v"
        }
        new CordysWiki().with {
            authenticate(configuration.wiki.userName, configuration.wiki.password)
            updateDropMergePage(configuration.wiki.pageId, resultingData, configuration.wiki.updateProductionServer)
        }
    }

    boolean getAllDependsOnTasksWereOK() {
        !allDependsOnTasks.any { Task t -> t.state.failure != null }
    }

    boolean getAnyDependsOnTasksDidWork() {
        allDependsOnTasks.any { Task t -> t.state.didWork }
    }

    Collection<Task> getAllDependsOnTasks() {
        updateAllTask.dependsOn.findAll { it instanceof Task }
    }

    public static JenkinsJob getJenkinsJob(com.opentext.dropmerge.dsl.JenkinsJob job) {
        Jenkins.getInstance(job.server.url).withJob(job.jobName, job.matrixAxes)
    }
}
