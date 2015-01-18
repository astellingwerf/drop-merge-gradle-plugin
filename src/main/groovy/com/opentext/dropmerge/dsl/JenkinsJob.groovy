package com.opentext.dropmerge.dsl

class JenkinsJob {
    String name
    String jobName
    JenkinsServer server
    String description
    Map<String, String> matrixAxes
    Set<JsonDataType> dataTypes = []

    JenkinsJob(String name) {
        this.name = name
    }

    void jobName(String jobName) { this.jobName = jobName }

    void server(JenkinsServer server) { this.server = server }

    void description(String description) { this.description = description }

    void matrixValues(Map<String, String> matrixAxes) { this.matrixAxes = matrixAxes }

    void addDataType(JsonDataType t) { dataTypes += t }

    @Override
    public String toString() {
        return "$name => '" + jobName + '\'' +
                " on " + server.name +
                ", description='" + description + '\'' +
                (matrixAxes ? ", matrixAxes=$matrixAxes" : '');
    }
}
