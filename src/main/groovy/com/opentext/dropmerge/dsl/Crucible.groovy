package com.opentext.dropmerge.dsl;

class Crucible {
    String userName
    String password
    String projectKey

    def userName(String userName) { this.userName = userName }

    def password(String password) { this.password = password }

    def project(String key) { this.projectKey = key }

    def promptForPassword() {
        def console = System.console()
        if (console) {
            password String.valueOf(console.readPassword(" > Please enter the Crucible password${userName ? '' : " for $userName"}: "))
        } else {
            throw new IllegalStateException('Cannot get console to read the Crucible password.')
        }
    }
}
