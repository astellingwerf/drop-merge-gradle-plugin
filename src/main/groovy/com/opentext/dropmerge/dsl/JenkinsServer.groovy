package com.opentext.dropmerge.dsl

class JenkinsServer {
    String name
    String url

    JenkinsServer(String name) {
        this.name = name
    }

    void url(String url) { this.url = url }

    @Override
    public String toString() {
        return name + ' => \'' + url + '\'';
    }
}
