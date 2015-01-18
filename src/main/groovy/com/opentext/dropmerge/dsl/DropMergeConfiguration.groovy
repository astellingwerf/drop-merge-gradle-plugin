package com.opentext.dropmerge.dsl

import org.gradle.api.NamedDomainObjectContainer

class DropMergeConfiguration {
    NamedDomainObjectContainer<JenkinsServer> jenkinsServers
    NamedDomainObjectContainer<JenkinsJob> jenkinsJobs
    NamedDomainObjectContainer<RegressionTest> regressionTests
    NamedDomainObjectContainer<QualityAndProcessQuestion> qualityAndProcessQuestions

    Team team = new Team()
    Wiki wiki = new Wiki()
    Crucible crucible = new Crucible()

    WipTrunkPair<JenkinsJob> pmd = new WipTrunkPair<JenkinsJob>()
    WipTrunkPair<JenkinsJob> mbv = new WipTrunkPair<JenkinsJob>()
    WipTrunkPair<JenkinsJob> compilerWarnings = new WipTrunkPair<JenkinsJob>()
    Collection<JenkinsJob> upgrade = []
    Collection<JenkinsJob> integrationTests = []

    DropMergeConfiguration(NamedDomainObjectContainer<JenkinsServer> jenkinsServers,
                           NamedDomainObjectContainer<JenkinsJob> jenkinsJobs,
                           NamedDomainObjectContainer<RegressionTest> regressionTests,
                           NamedDomainObjectContainer<QualityAndProcessQuestion> qualityAndProcessQuestions) {
        this.jenkinsServers = jenkinsServers
        this.jenkinsJobs = jenkinsJobs
        this.regressionTests = regressionTests
        this.qualityAndProcessQuestions = qualityAndProcessQuestions
    }

    def team(Closure closure) {
        team.with(closure)
    }

    def wiki(Closure closure) {
        wiki.with(closure)
    }

    def crucible(Closure closure) {
        crucible.with(closure)
    }

    def jenkinsServers(Closure closure) {
        jenkinsServers.configure(closure)
    }

    def jenkinsJobs(Closure closure) {
        jenkinsJobs.configure(closure)
    }

    def regressionTests(Closure closure) {
        regressionTests.configure(closure)
        regressionTests.each {
            it.comparables.each { [it.left, it.right].each { it*.addDataType JsonDataType.Tests } }
            it.others*.addDataType JsonDataType.Tests
        }
    }

    def pmd(Closure closure) {
        pmd.with(closure)
        pmd.trunk.addDataType JsonDataType.PMD
        pmd.wip.addDataType JsonDataType.PMD
    }

    def compilerWarnings(Closure closure) {
        compilerWarnings.with(closure)
        compilerWarnings.trunk.addDataType JsonDataType.CompilerWarnings
        compilerWarnings.wip.addDataType JsonDataType.CompilerWarnings
    }

    def mbv(Closure closure) {
        mbv.with(closure)
        mbv.trunk.addDataType JsonDataType.MBV
        mbv.wip.addDataType JsonDataType.MBV
    }

    def upgrade(JenkinsJob job) {
        upgrade([job])
    }

    def upgrade(Collection<JenkinsJob> jobs) {
        this.upgrade.addAll(jobs)
    }

    def integrationTests(JenkinsJob job) {
        integrationTests([job])
    }

    def integrationTests(Collection<JenkinsJob> jobs) {
        this.integrationTests.addAll(jobs)
    }

    def qualityQuestions(Closure closure) {
        qualityAndProcessQuestions.configure(closure)
    }

    @Override
    public String toString() {
        return "DropMergeConfiguration {\n" +
                "\tteam {\n\t\t" + team +
                "\n\t}, jenkinsServers {\n" + jenkinsServers.collect { '\t\t' + it.toString() }.join('\n') +
                "\n\t}, jenkinsJobs {\n" + jenkinsJobs.collect { '\t\t' + it.toString() }.join('\n') +
                "\n\t}, regressionTests {\n" + regressionTests.collect { '\t\t' + it.toString() }.join('\n') +
                "\n\t}, pmd {\n\t\t" + pmd +
                "\n\t}, compilerWarnings {\n\t\t" + compilerWarnings +
                "\n\t}, upgrade {\n\t\t" + upgrade.collect { it.name } +
                "\n\t}, integrationTests {\n\t\t" + integrationTests.collect { it.name } +
                '\n\t}\n}';
    }
}
