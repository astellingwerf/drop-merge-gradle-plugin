package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.RegressionTest
import com.opentext.dropmerge.jenkins.JenkinsJob
import com.opentext.dropmerge.wiki.WikiTableBuilder
import groovy.time.TimeCategory
import groovy.xml.MarkupBuilder
import org.gradle.api.tasks.TaskAction
import org.jenkinsci.images.IconCSS

import java.text.SimpleDateFormat

import static com.opentext.dropmerge.jenkins.TestCount.*
import static com.opentext.dropmerge.jenkins.Util.getJenkinsUrlWithStatus
import static com.opentext.dropmerge.tasks.UpdateWiki.getJenkinsJob

class SuccessfulRegressionTestsComment extends SimpleField {

    @TaskAction
    void createTables() {
        result = WikiTableBuilder.table { WikiTableBuilder table ->
            table.setHeaders(['Type', 'OS', 'Successful', 'Failed', 'Skipped', 'Link'])

            int passCount = 0, failCount = 0, skipCount = 0
            config.regressionTests.collectEntries { RegressionTest tests ->
                [(tests.name): tests.comparables.collectMany { it.left } + tests.others]
            }.each { String type, Collection<com.opentext.dropmerge.dsl.JenkinsJob> jobs ->
                jobs.each { com.opentext.dropmerge.dsl.JenkinsJob job ->
                    JenkinsJob jj = getJenkinsJob(job)
                    passCount += jj.getTestFigure(Pass) as int
                    failCount += jj.getTestFigure(Fail) as int
                    skipCount += jj.getTestFigure(Skip) as int
                    table << [type,
                              job.description,
                              jj.getTestFigure(Pass),
                              jj.getTestFigure(Fail),
                              jj.getTestFigure(Skip),
                              getJenkinsUrlWithStatus(jj)
                    ]
                }
            }
            table << ['All', 'All', "$passCount", "$failCount", "$skipCount", '']
        } + WikiTableBuilder.table { WikiTableBuilder table ->
            config.regressionTests.each { RegressionTest tests ->
                tests.comparables.each {
                    String wipDescription = it.left*.description.unique().join(' / ')
                    it.right.each { com.opentext.dropmerge.dsl.JenkinsJob job ->
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
        } + WikiTableBuilder.withHtml { MarkupBuilder html ->
            html.style IconCSS.style
        }
    }
}
