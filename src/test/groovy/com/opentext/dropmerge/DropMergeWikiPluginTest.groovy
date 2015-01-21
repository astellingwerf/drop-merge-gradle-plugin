package com.opentext.dropmerge

import com.opentext.dropmerge.dsl.DropMergeConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class DropMergeWikiPluginTest {

    static String COMMENT = 'Comment'
    static String MULTI_WORD_COMMENT = 'Comment might have multiple words.'

    @Test
    public void simpleTest() {
        Project project = ProjectBuilder.builder().build()
        project.with {
            apply plugin: com.opentext.dropmerge.DropMergeWikiPlugin

            dropMerge {
                team {
                    name 'Platform core team'
                    scrumMaster 'gjansen'
                    architect 'wjgerrit', 'broos'
                    productManager 'jpluimer'
                    otherMembers 'astellingwerf', 'dkwakkel', 'gligtenb', 'jrosman', 'rdouden'
                }

                jenkinsServers {
                    buildmasterNL { url 'http://buildmaster-nl.vanenburg.com/jenkins' }
                    buildmasterHYD { url = 'http://buildmaster-hyd.vanenburg.com/jenkins' }
                }

                jenkinsJobs {
                    'pct-trunk-mb' { on jenkinsServers.buildmasterNL }
                    trunkMb1 { jobName = 'pct-trunk-mb'; server = jenkinsServers.buildmasterNL }
                    trunkMb2 { jobName 'pct-trunk-mb' on jenkinsServers.buildmasterNL }
                }

                qualityQuestions {
                    a { answer 'No'; comment COMMENT }
                    b { answer 'No', COMMENT }
                    c { no COMMENT }
                    d { yes COMMENT }
                    e { not tested 'Comment' }
                    f { not applicable 'Comment might have multiple words.' }
                    g { not(applicable).getProperty(MULTI_WORD_COMMENT) }
                }
            }
        }

        assertTrue(project.extensions.dropMerge instanceof DropMergeConfiguration)
        def dm = (DropMergeConfiguration) project.extensions.dropMerge

        NamedDomainObjectContainer jobs = dm.jenkinsJobs
        assertEquals(3, jobs.size())
        assertTrue(jobs.every { it == jobs.first() })

        NamedDomainObjectContainer qq = dm.qualityAndProcessQuestions
        assert qq.collect { it.answer } == ['No', 'No', 'No', 'Yes', 'Not tested', 'Not applicable', 'Not applicable']
        assert qq.collect { it.comment } == [COMMENT] * 5 + [MULTI_WORD_COMMENT] * 2
    }

}
