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
        this.jobName = name
    }

    JenkinsJob jobName(String jobName) { this.jobName = jobName; this }

    JenkinsJob server(JenkinsServer server) { this.server = server; this }

    JenkinsJob on(JenkinsServer server) { this.server = server; this }

    JenkinsJob description(String description) { this.description = description; this }

    JenkinsJob matrixValues(Map<String, String> matrixAxes) { this.matrixAxes = matrixAxes; this }

    void addDataType(JsonDataType t) { dataTypes += t }

    @Override
    public String toString() {
        return "$name => '" + jobName + '\'' +
                " on " + server.name +
                ", description='" + description + '\'' +
                (matrixAxes ? ", matrixAxes=$matrixAxes" : '');
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        JenkinsJob that = (JenkinsJob) o

        if (jobName != that.jobName) return false
        if (matrixAxes != that.matrixAxes) return false
        if (server != that.server) return false

        return true
    }

    int hashCode() {
        int result
        result = jobName.hashCode()
        result = 31 * result + server.hashCode()
        result = 31 * result + (matrixAxes != null ? matrixAxes.hashCode() : 0)
        return result
    }
}
