package com.opentext.dropmerge.dsl


class Team {
    String name
    List<String> scrumMaster = []
    List<String> architects = []
    List<String> productManager = []
    List<String> otherMembers = []

    void name(String name) { this.name = name }

    void scrumMaster(String... scrumMaster) { this.scrumMaster.addAll(scrumMaster) }

    void architect(String... architect) { this.architects.addAll(architect) }

    void productManager(String... productManager) { this.productManager.addAll(productManager) }

    void otherMembers(String... member) { this.otherMembers.addAll(member) }

    List<String> getAllMembers() {
        (scrumMaster + productManager + architects + otherMembers).sort().unique()
    }

    @Override
    public String toString() {
        return name + ': ' +
                allMembers.collect {
                    def roles = [A: this.&getArchitects,
                                 P: this.&getProductManager,
                                 S: this.&getScrumMaster].collectMany { role ->
                        (role.value().contains(it) ? [role.key] : [])
                    }
                    it + (roles ? " (${roles.join(',')})" : '')
                }.join(', ')
    }
}
