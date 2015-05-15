package com.opentext.dropmerge.dsl

import org.apache.commons.lang3.tuple.Pair

import java.util.regex.Pattern

// TODO: Justifications
// TODO: Exclusion rules
class RegressionTest {
    String name

    Collection<Pair<Collection<JenkinsJob>, Collection<JenkinsJob>>> comparables = []
    Collection<JenkinsJob> others = []
    Collection<Closure<Boolean>> exclusions = []

    RegressionTest(String name) {
        this.name = name
    }

    void compare(JenkinsJob wip, JenkinsJob trunk) {
        compare([wip], [trunk])
    }

    void compare(Collection<JenkinsJob> wip, Collection<JenkinsJob> trunk) {
        comparables += Pair.of(wip, trunk)
    }

    void others(JenkinsJob... others) {
        this.others.addAll(others)
    }

    void exclude(Pattern pattern) {
        exclude { String className -> pattern.matcher(className).matches() }
    }

    void exclude(Closure<Boolean> closure) {
        exclusions += closure
    }

    @Override
    public String toString() {
        return "$name => comparables=" + comparables.collect {
            Pair.of(it.left.collect { it.name }, it.right.collect { it.name })
        } +
                ", others=" + others.collect { it.name }
    }
}
