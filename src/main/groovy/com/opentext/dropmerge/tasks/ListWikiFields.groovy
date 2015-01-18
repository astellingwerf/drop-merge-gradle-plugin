package com.opentext.dropmerge.tasks

import com.opentext.dropmerge.dsl.DropMergeConfiguration
import com.opentext.dropmerge.tasks.updatewiki.SimpleField
import com.opentext.dropmerge.wiki.CordysWiki
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ListWikiFields extends DefaultTask {

    DropMergeConfiguration getConfiguration() {
        return project.extensions.dropMerge
    }

    @TaskAction
    public void printWikiFields() {
        Map<String, String> fieldNamesOnTasks = [:]
        UpdateWiki.allDependsOnTasks.findAll {
            it instanceof SimpleField
        }.sort { it.name }.each { SimpleField sf -> sf.fieldNames.collect { fieldNamesOnTasks.put(it, sf.name) } }

        Set<String> fieldNamesOnWiki = new CordysWiki().with {
            authenticate(configuration.wiki.userName, configuration.wiki.password)
            getDropMergeFields(configuration.wiki.pageId).keySet()
        }

        def suffixes = ['Before', 'After', 'Comment']
        int suffixMaxWidth = suffixes*.size().max()
        int maxFieldNameWidth = (fieldNamesOnWiki + fieldNamesOnTasks.keySet())*.size().max()


        println 'Fields covered by tasks:'
        fieldNamesOnTasks.each { String fieldName, String task ->
            int i = suffixMaxWidth - (suffixes.find { fieldName =~ /$it$/ }?.size() ?: 0)
            println fieldName.padRight(fieldName.size() + i).padLeft(maxFieldNameWidth) + ': ' + task
        }
        println()
        println 'Fields not covered by tasks:'
        (fieldNamesOnWiki - fieldNamesOnTasks.keySet()).each { println "- $it" }

    }
}
