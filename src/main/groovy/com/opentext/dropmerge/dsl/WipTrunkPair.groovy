package com.opentext.dropmerge.dsl


class WipTrunkPair<T> {
    T trunk, wip

    void trunk(T value) { trunk = value }

    void wip(T value) { wip = value }

    @Override
    public String toString() {
        return "trunk=$trunk, wip=$wip"
    }
}
