package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.JenkinsJob
import org.gradle.api.tasks.TaskAction

import static com.opentext.dropmerge.jenkins.JenkinsJob.LAST_COMPLETED_BUILD
import static com.opentext.dropmerge.jenkins.Util.getJenkinsUrlWithStatus
import static com.opentext.dropmerge.tasks.UpdateWiki.getJenkinsJob
import static com.opentext.dropmerge.wiki.WikiTableBuilder.withHtml

class JenkinsJobStatus extends SimpleFieldWithComment {
    Closure<Collection<JenkinsJob>> selector

    void set(Closure<Collection<JenkinsJob>> s) {
        selector = s
    }

    @TaskAction
    def createComment() {
        selectedOption = jobs.every {
            getJenkinsJob(it).lastBuildResult == 'SUCCESS'
        } ? 'Yes' : 'No'

        comment = withHtml { html ->
            html.p {
                jobs.each { JenkinsJob j ->
                    getJenkinsUrlWithStatus(getJenkinsJob(j), LAST_COMPLETED_BUILD, 'Job').with {
                        it.delegate = html
                        it.call()
                    }
                    if (j.description) {
                        html.mkp.yield ' ' + j.description
                    }
                    html.br()
                }
            }
        }
    }

    Collection<JenkinsJob> getJobs() {
        return selector();
    }

}
