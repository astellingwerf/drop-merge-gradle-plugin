package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.RegressionTest
import com.opentext.dropmerge.tasks.UpdateWiki
import com.opentext.dropmerge.wiki.WikiTableBuilder
import org.gradle.api.tasks.TaskAction

import static com.opentext.dropmerge.jenkins.Jenkins.getTestDiffsPerSuite

class TotalRegressionTestsComment extends SimpleField {
    @TaskAction
    void createTable() {
        result = WikiTableBuilder.table { table ->
            config.regressionTests.each { RegressionTest tests ->
                tests.comparables.each {
                    String wipOS = it.left*.description.unique().join(' / ')
                    getTestDiffsPerSuite(
                            it.right.collect(UpdateWiki.&getJenkinsJob),
                            it.left.collect(UpdateWiki.&getJenkinsJob)).each { suite, difference ->
                        table.addRow(
                                'Suite / Test': suite,
                                'Difference': String.format('%+d', difference),
                                'Type': tests.name,
                                'OS': wipOS,
                                //TODO Justifications
                        )
                    }
                }
            }
        }
    }
}
