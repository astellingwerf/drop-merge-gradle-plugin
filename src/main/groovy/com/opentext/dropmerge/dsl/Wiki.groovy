package com.opentext.dropmerge.dsl;

class Wiki {
    String userName
    String password
    String pageId
    boolean updateProductionServer = true

    def userName(String userName) { this.userName = userName }

    def password(String password) { this.password = password }

    def pageId(String id) { this.pageId = id }

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
}
