package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.RegressionTest
import com.opentext.dropmerge.dsl.JenkinsJob as JobInDsl
import com.opentext.dropmerge.jenkins.JenkinsJob
import com.opentext.dropmerge.tasks.jenkins.UpdateResponseCache
import com.opentext.dropmerge.wiki.WikiTableBuilder
import groovy.time.TimeCategory
import groovy.xml.MarkupBuilder
import org.gradle.api.tasks.TaskAction
import org.jenkinsci.images.IconCSS

import java.text.SimpleDateFormat

import static com.opentext.dropmerge.jenkins.TestCount.*
import static com.opentext.dropmerge.jenkins.Util.getJenkinsUrlWithStatus

class SuccessfulRegressionTestsComment extends SimpleField {

    SuccessfulRegressionTestsComment() {
        dependsOn {
            UpdateResponseCache.getTaskNames(config.regressionTests.collectMany { RegressionTest tests ->
                tests.comparables.collectMany { it.left + it.right } + tests.others
            })
        }
    }

    @TaskAction
    void createTables() {
        result = WikiTableBuilder.table { WikiTableBuilder table ->
            table.setHeaders(['Type', 'OS', 'Successful', 'Failed', 'Skipped', 'Link'])

            int passCount = 0, failCount = 0, skipCount = 0
            config.regressionTests.collectEntries { RegressionTest tests ->
                Collection<JobInDsl> jobs = tests.comparables.collectMany { it.left } + tests.others
                jobs.each { JobInDsl job ->
                    JenkinsJob jj = getJenkinsJob(job)
                    passCount += jj.getTestFigure(Pass, tests.exclusions)
                    failCount += jj.getTestFigure(Fail, tests.exclusions)
                    skipCount += jj.getTestFigure(Skip, tests.exclusions)
                    table << [tests.name,
                              job.description,
                              jj.getTestFigure(Pass, tests.exclusions),
                              jj.getTestFigure(Fail, tests.exclusions),
                              jj.getTestFigure(Skip, tests.exclusions),
                              getJenkinsUrlWithStatus(jj)
                    ]
                }
            }
            table << ['All', 'All', "$passCount", "$failCount", "$skipCount", '']
            return
        } + WikiTableBuilder.table { WikiTableBuilder table ->
            config.regressionTests.each { RegressionTest tests ->
                tests.comparables.each {
                    String wipDescription = it.left*.description.unique().join(' / ')
                    it.right.each { JobInDsl job ->
                        JenkinsJob jj = getJenkinsJob(job)
                        Date ts = jj.getBuildTimestamp(JenkinsJob.LAST_COMPLETED_BUILD)
                        String timestampText = new SimpleDateFormat('MMM dd \'at\' HH:mm z').format(ts)
                        def diff = TimeCategory.minus(new Date(), ts).days
                        if (diff > 2)
                            timestampText += ", $diff days ago"
                        table << ['Type'                         : tests.name,
                                  'OS'                           : wipDescription,
                                  'WIP was compared to trunk job': getJenkinsUrlWithStatus(jj),
                                  'Timestamp'                    : timestampText
                        ]
                    }
                }
            }
            return
        } + WikiTableBuilder.withHtml { MarkupBuilder html ->
            html.style IconCSS.style
        }
    }
}
