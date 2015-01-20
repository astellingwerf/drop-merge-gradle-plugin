package com.opentext.dropmerge

import com.opentext.dropmerge.dsl.DropMergeConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class DropMergeWikiPluginTest {

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
            }
        }

        assertTrue(project.extensions.dropMerge instanceof DropMergeConfiguration)
        NamedDomainObjectContainer jobs = ((DropMergeConfiguration)project.extensions.dropMerge).jenkinsJobs
        assertEquals(3, jobs.size())
        assertTrue(jobs.every { it == jobs.first() } )
    }

}
