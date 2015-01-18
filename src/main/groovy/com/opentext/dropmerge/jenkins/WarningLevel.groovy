package com.opentext.dropmerge.jenkins

public enum WarningLevel implements com.opentext.dropmerge.jenkins.JenkinsJsonField {
    High, Normal

    @Override
    String allValues() {
        values().collect { it.jsonField }.join(',')
    }

    @Override
    String getJsonField() {
        'numberOf' + this.name() + 'PriorityWarnings'
    }
}