package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.JenkinsJob as JobInDsl
import com.opentext.dropmerge.dsl.WipTrunkPair
import com.opentext.dropmerge.jenkins.JenkinsJob

class PMDCount extends QualityMetricCount {
    String reportTitle = 'PMD results'

    @Override
    protected WipTrunkPair<JobInDsl> getMetricPair() {
        config.pmd
    }

    @Override
    protected String getMetricFigure(JenkinsJob job) {
        job.getPMDFigure(level)
    }

    @Override
    protected getReport(JenkinsJob job) {
        job.PMDReport
    }

    @Override
    protected String getReportUrl() {
        "pmdResult/$upperCaseLevel"
    }
}
