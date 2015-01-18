package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.JenkinsJob as JobInDsl
import com.opentext.dropmerge.dsl.WipTrunkPair
import com.opentext.dropmerge.jenkins.JenkinsJob

class MBVCount extends QualityMetricCount {
    String reportTitle = 'MBV results'

    @Override
    protected WipTrunkPair<JobInDsl> getMetricPair() {
        config.mbv
    }

    @Override
    protected String getMetricFigure(JenkinsJob job) {
        job.getMBFigure(level)
    }

    @Override
    protected getReport(JenkinsJob job) {
        job.MBVReport
    }

    @Override
    protected String getReportUrl() {
        "muvipluginResult/$upperCaseLevel"
    }

    // MBV comment deviates from the standard, so we have to overwrite get- and setComment.
    String getCommentKey() {
        'Multibrowser' + (fieldName - ~/^MB/) + 'Comment'
    }

    @Override
    String getComment() {
        results[commentKey]
    }

    @Override
    void setComment(String comment) {
        results[commentKey] = comment
    }


}
