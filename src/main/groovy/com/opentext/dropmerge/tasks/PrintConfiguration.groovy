package com.opentext.dropmerge.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class PrintConfiguration extends DefaultTask {

    @TaskAction
    public void printConfiguration() {
        println project.extensions.dropMerge
    }
}
