package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.JenkinsJob as JobInDsl
import com.opentext.dropmerge.dsl.WipTrunkPair
import com.opentext.dropmerge.jenkins.JenkinsJob

class CWCount extends QualityMetricCount {
    String reportTitle = 'Compiler warnings'
    String reportUrl = 'warnings3Result'
    
    @Override
    protected WipTrunkPair<JobInDsl> getMetricPair() {
        config.compilerWarnings
    }

    @Override
    protected String getMetricFigure(JenkinsJob job) {
        job.compilerWarningFigure
    }

    @Override
    protected getReport(JenkinsJob job) {
        job.compilerWarningsReport
    }
}
