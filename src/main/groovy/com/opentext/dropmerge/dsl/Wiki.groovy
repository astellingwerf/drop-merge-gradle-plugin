package com.opentext.dropmerge.dsl;

import com.opentext.dropmerge.wiki.CordysWiki
import java.util.regex.Pattern

class Wiki {
    String userName
    String password
    Closure<String> pageId
    boolean updateProductionServer = true

    def userName(String userName) { this.userName = userName }

    def password(String password) { this.password = password }

    def pageId(String id) { this.pageId = { id } }

    def pageId(int id) { pageId("$id") }

    def updateProductionServer(boolean updateProductionServer) { this.updateProductionServer = updateProductionServer }

    def promptForPassword() {
        def console = System.console()
        if (console) {
            password String.valueOf(console.readPassword(" > Please enter the wiki password${userName ? '' : " for $userName"}: "))
        } else {
            throw new IllegalStateException('Cannot get console to read the wiki password.')
        }
    }

    String getPageId() { return pageId() }

    def findPageId(int parentId, Pattern namePattern, int patternCaptureGroup = 1) {
        pageId = {
            CordysWiki.getInstance(userName, password).getContent(parentId).children.content
                    .collectEntries { [(it.@id): namePattern.matcher(it.title.text())] }
                    .findAll { id, matcher -> matcher.find() }
                    .collectEntries { id, matcher -> [(id): matcher.group(patternCaptureGroup)] }
                    .groupBy { it.value.takeWhile { Character.isDigit(it) } as int }
                    .max { it.key }.value
                    .max { it.value }
                    .key
        }.memoize()
    }
}
