package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.JenkinsJob as JobInDsl
import com.opentext.dropmerge.dsl.WipTrunkPair
import com.opentext.dropmerge.jenkins.Jenkins
import com.opentext.dropmerge.jenkins.JenkinsJob
import com.opentext.dropmerge.jenkins.WarningLevel
import com.opentext.dropmerge.wiki.WikiTableBuilder
import org.gradle.api.tasks.TaskAction

import static com.opentext.dropmerge.jenkins.JenkinsJob.LAST_SUCCESSFUL_BUILD
import static com.opentext.dropmerge.tasks.UpdateWiki.getJenkinsJob
import static com.opentext.dropmerge.wiki.WikiTableBuilder.withHtml

abstract class QualityMetricCount extends SimpleFieldWithComment {

    WarningLevel level = WarningLevel.Normal

    void set(WarningLevel l) {
        level = l
    }

    protected List<String> getPriorities() { [upperCaseLevel] }

    protected String getUpperCaseLevel() { level.name().toUpperCase() }

    @TaskAction
    public void calculateTestCount() {
        def labelToProjection = [Before: { it.trunk }, After: { it.wip }]
        didWork = labelToProjection.values().every { projection -> projection(metricPair) != null }
        if (!didWork)
            return
        labelToProjection.each { appendix, projection ->
            setResult appendix, getMetricFigure(getJenkinsJob(projection(metricPair)))
        }
    }

    protected abstract WipTrunkPair<JobInDsl> getMetricPair()

    protected abstract String getMetricFigure(JenkinsJob job)

    @TaskAction
    public void createDiffTable() {
        comment = withHtml { html ->
            html.p {
                b 'Trunk:'
                mkp.yieldUnescaped '&nbsp;'
                a(href: getJenkinsJob(metricPair.trunk).getBuildUrl(LAST_SUCCESSFUL_BUILD) + '/' + reportUrl + '/', reportTitle)
                mkp.yield ' from our own trunk build.'
                br()
                b 'WIP:'
                mkp.yieldUnescaped '&nbsp;'
                a(href: getJenkinsJob(metricPair.wip).getBuildUrl(LAST_SUCCESSFUL_BUILD) + '/' + reportUrl + '/', reportTitle)
                mkp.yield ' from our WIP build.'
            }
        }
        comment += WikiTableBuilder.table { table ->
            Jenkins.DifferenceDetails differenceDetails = Jenkins.getDetailedDiffsPerSuite(
                    getReport(getJenkinsJob(metricPair.trunk)),
                    getReport(getJenkinsJob(metricPair.wip)),
                    priorities)
            differenceDetails.diffsPerSuite.each buildDiffTable(table, differenceDetails, reportUrl)
        }
    }

    protected abstract def getReport(JenkinsJob job)

    protected abstract String getReportUrl()

    protected abstract String getReportTitle()

    Closure buildDiffTable(WikiTableBuilder table, Jenkins.DifferenceDetails diffDetails, String reportUrl) {
        return { filename, difference ->
            String linkTrunk, linkWip
            boolean b2a, a2b;

            if ((b2a = diffDetails.beforeToAfter.containsKey(filename)) || diffDetails.onlyBefore.contains(filename)) {
                linkTrunk = getFileReportUrl(getJenkinsJob(metricPair.trunk), reportUrl, filename)
                if (b2a) {
                    linkWip = getFileReportUrl(getJenkinsJob(metricPair.wip), reportUrl, diffDetails.beforeToAfter[filename])
                }
            } else if ((a2b = diffDetails.afterToBefore.containsKey(filename)) || diffDetails.onlyAfter.contains(filename)) {
                linkWip = getFileReportUrl(getJenkinsJob(metricPair.wip), reportUrl, filename)
                if (a2b) {
                    linkTrunk = getFileReportUrl(getJenkinsJob(metricPair.trunk), reportUrl, diffDetails.afterToBefore[filename])
                }
            }

            table.addRow(
                    'File': filename,
                    'Link': {
                        nobr {
                            if (linkTrunk)
                                a(href: linkTrunk, 'T')
                            if (linkTrunk && linkWip)
                                mkp.yield ' / '
                            if (linkWip)
                                a(href: linkWip, 'W')
                        }
                    },
                    'Diff': String.format('%+d', difference))
        }
    }

    private String getFileReportUrl(JenkinsJob job, String reportUrl, String fileName) {
        return "${job.getBuildUrl(LAST_SUCCESSFUL_BUILD)}/$reportUrl/file.${fileName.hashCode()}/"
    }

}
