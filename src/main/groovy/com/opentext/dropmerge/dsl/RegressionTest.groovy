package com.opentext.dropmerge.dsl

import org.apache.commons.lang3.tuple.Pair

// TODO: Justifications
// TODO: Exclusion rules
class RegressionTest {
    String name

    Collection<Pair<Collection<JenkinsJob>, Collection<JenkinsJob>>> comparables = []
    Collection<JenkinsJob> others = []

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

    @Override
    public String toString() {
        return "$name => comparables=" + comparables.collect {
            Pair.of(it.left.collect { it.name }, it.right.collect { it.name })
        } +
                ", others=" + others.collect { it.name }
    }
}
