package com.opentext.dropmerge

import com.opentext.dropmerge.dsl.DropMergeConfiguration
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

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
                    buildmasterHYD { url 'http://buildmaster-hyd.vanenburg.com/jenkins' }
                    jenkinsOfSVT { url 'http://srv-ind-svt9l.vanenburg.com:8080' }
                    jenkinsOfCMT { url 'http://cmt-jenkins.vanenburg.com/jenkins' }
                }
            }
        }

        assertTrue(project.extensions.dropMerge instanceof DropMergeConfiguration)
    }

}
