package com.opentext.dropmerge.tasks.updatewiki

import com.opentext.dropmerge.dsl.DropMergeConfiguration
import com.opentext.dropmerge.wiki.CordysWiki
import org.gradle.api.DefaultTask

public class SimpleField extends DefaultTask {
    String fieldName
    Map<String, String> results

    DropMergeConfiguration getConfig() {
        return project.extensions.dropMerge
    }

    def getFormField() {
        def wiki = new CordysWiki()
        wiki.authenticate(config.wiki.userName, config.wiki.password)
        wiki.getDropMergeFields(config.wiki.pageId)[fieldName]
    }

    void setResult(String fieldNameAppendix = '', String value) {
        results[fieldName + fieldNameAppendix] = value
    }

    String getResult(String fieldNameAppendix = '') {
        results[fieldName + fieldNameAppendix]
    }

    void setSelectedOption(String option) {
        result = CordysWiki.selectOption(formField.rawItem, option)
    }

}
