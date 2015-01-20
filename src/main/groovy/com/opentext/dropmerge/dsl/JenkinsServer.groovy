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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        JenkinsServer that = (JenkinsServer) o

        return url == that.url
    }

    int hashCode() {
        return url.hashCode()
    }
}
