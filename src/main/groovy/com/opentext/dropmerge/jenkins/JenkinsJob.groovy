package com.opentext.dropmerge.jenkins

import groovy.json.JsonSlurper
import groovy.transform.Memoized
import org.gradle.api.logging.Logger

import java.text.SimpleDateFormat

class JenkinsJob {
    public static final String LAST_BUILD = 'lastBuild'
    public static final String LAST_COMPLETED_BUILD = 'lastCompletedBuild'
    public static final String LAST_SUCCESSFUL_BUILD = 'lastSuccessfulBuild'

	Logger logger
    ResponseReader responseReader

    private final Jenkins onInstance;
    private final String name
    private final Map<String, String> matrixAxes

    JenkinsJob(Jenkins onInstance, String name, Map<String, String> matrixAxes = null, ResponseReader responseReader = null) {
        this.onInstance = onInstance
        this.name = name
        this.matrixAxes = matrixAxes

        this.responseReader = responseReader ?: new ResponseCacheReader()

        if (matrixAxes) {
            List<String[]> matches = jsonForJob(null, null, 'activeConfigurations[name]')['activeConfigurations']
                    .collect { it.name.split(',') }
                    .findAll { String[] configuration ->
                matrixAxes.every { String axis, String value ->
                    configuration.contains("$axis=$value")
                }
            }
            if (matches.size() == 0)
                throw new IllegalArgumentException("No configuration matches $matrixAxes for job $this");
            if (matches.size() > 1)
                throw new IllegalArgumentException("Multiple configuration matches $matrixAxes for job $this");

            this.name += '/' + matches.first().join(',')
        }
    }

    public String getLastBuildResult() {
        jsonForJob(LAST_COMPLETED_BUILD, null, 'result')['result'].toString()
    }

    private String getPropertyOfJobWithinReports(String report, String prop) {
        jsonForJob(report, prop)[prop].toString()
    }

    private String getPropertyOfJobWithinReports(String report, JenkinsJsonField prop) {
        jsonForJob(report, prop)[prop.jsonField].toString()
    }

    public def getTestReport() {
        jsonForJob(LAST_SUCCESSFUL_BUILD, 'testReport', 'suites[name,cases[status]]')
    }

    public def getPMDReport() {
        jsonForJob(LAST_SUCCESSFUL_BUILD, 'pmdResult', 'warnings[priority,fileName]')
    }

    public String getPMDFigure(WarningLevel level) {
        getPropertyOfJobWithinReports('pmdResult', level)
    }

    public String getCompilerWarningFigure() {
        getPropertyOfJobWithinReports('warnings3Result', 'numberOfWarnings')
    }

    public String getMBFigure(WarningLevel level) {
        getPropertyOfJobWithinReports('muvipluginResult', level)
    }

    public def getMBVReport() {
        jsonForJob(LAST_SUCCESSFUL_BUILD, 'muvipluginResult', 'warnings[priority,fileName]')
    }

    public def getCompilerWarningsReport() {
        jsonForJob(LAST_SUCCESSFUL_BUILD, 'warnings3Result', 'warnings[priority,fileName]')
    }

    private def jsonForJob(String build, String subPage, String jsonPath, Integer depth = null) {
        String url = [getBuildUrl(build), subPage, 'api', 'json'].findAll { it != null }.join('/')
        if (jsonPath) url += "?tree=$jsonPath"
        else if (depth) url += "?depth=$depth"

        return slurpJson(url, logger, responseReader)
    }

    @Memoized
    private static def slurpJson(String url, Logger logger, ResponseReader reader) {
        new JsonSlurper().parseText(getText(url, logger, reader))
    }

    @Memoized
    private static String getText(String url, Logger logger, ResponseReader reader) {
        reader.getText(url, logger)
    }

    private def jsonForJob(String subPage, String jsonPath) {
        jsonForJob(LAST_SUCCESSFUL_BUILD, subPage, jsonPath)
    }

    private def jsonForJob(String subPage, JenkinsJsonField jsonPath) {
        jsonForJob(subPage, jsonPath.allValues())
    }

    public String getJobUrl() {
        "$onInstance.rootUrl/job/$name"
    }

    public String getBuildUrl(String build) {
        if (!build)
            jobUrl
        else
            "$jobUrl/$build"
    }

    public List<JenkinsJob> getMatrixSubJobs() {
        return jsonForJob(null, null, 'activeConfigurations[name]')['activeConfigurations'].collect {
            onInstance.withJob("$name/${it.name}", null, responseReader)
        }
    }

    public String getColor() {
        return jsonForJob(null, null, 'color')['color'].toString()
    }

    public Date getBuildTimestamp(String build) {
        final String format = 'yyMMddHHmmssZ'
        new SimpleDateFormat(format).parse(getText(getBuildUrl(build) + "/buildTimestamp?format=$format", logger, responseReader))
    }

    @Override
    public String toString() {
        String n
        if (matrixAxes) {
            n = "'${name.takeWhile { it != '/' }}' with $matrixAxes"
        } else {
            n = "'$name'"
        }
        return "$n on " + (new URL(onInstance.rootUrl).host - ~/\.vanenburg\.com$/);
    }
    
    public int getTestFigure(TestCount testCount, Collection<Closure<Boolean>> suiteExclusions) {
        if (matrixSubJobs.isEmpty())
            return getTestCount(suiteExclusions)[testCount] ?: 0
        else
            return matrixSubJobs.sum { it.getTestFigure(testCount, suiteExclusions) }
    }

    public Map<TestCount, Integer> getTestCount(Collection<Closure<Boolean>> suiteExclusions) {
        getTestCountBySuite(suiteExclusions).inject([:]) { result, kvp ->
            Map<TestCount, Integer> newResult = kvp.value.keySet().collectEntries { [(it): 0] } + result
            kvp.value.each { k, v -> newResult[k] += v }
            return newResult
        }
    }

    @Memoized
    public Map<String, Map<TestCount, Integer>> getTestCountBySuite(Collection<Closure<Boolean>> suiteExclusions) {
        return testReport.suites
                .collectEntries { suite ->
            if (!suiteExclusions.any { exclude -> exclude(suite.name) })
                [(suite.name): (suite.cases.countBy { c ->
                    switch (c.status) {
                        case 'FAILED': return TestCount.Fail
                        case 'SKIPPED': return TestCount.Skip
                        case 'PASSED': return TestCount.Pass
                    }
                })]
            else
                [:]
        }
        .findAll { it.value*.value.sum() > 0 }
    }
}
