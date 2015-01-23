package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.RegressionTest
import com.opentext.dropmerge.jenkins.TestCount
import com.opentext.dropmerge.tasks.jenkins.UpdateResponseCache
import org.gradle.api.tasks.TaskAction

class ComparableTestCount extends SimpleField {
    TestCount testCount

    void set(TestCount tc) {
        testCount = tc
    }

    ComparableTestCount() {
        dependsOn {
            UpdateResponseCache.getTaskNames(config.regressionTests.collectMany { RegressionTest tests ->
                tests.comparables.collectMany { it.left + it.right }
            })
        }
    }

    @TaskAction
    public void calculateTestCount() {
        [Before: { it.right }, After: { it.left }].each { appendix, projection ->
            setResult appendix, String.valueOf(config.regressionTests.sum { RegressionTest tests ->
                tests.comparables.collectMany(projection).sum {
                    getJenkinsJob(it).getTestFigure(testCount) as int
                }
            })
        }
    }

    @Override
    Collection<String> getFieldNames() {
        ['Before', 'After'].collect { it -> "$fieldName$it" }
    }
}
