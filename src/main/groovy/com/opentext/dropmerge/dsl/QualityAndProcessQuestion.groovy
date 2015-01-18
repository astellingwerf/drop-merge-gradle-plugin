package com.opentext.dropmerge.dsl

class QualityAndProcessQuestion {
    String name
    String answer
    String comment

    QualityAndProcessQuestion(String name) {
        this.name = name.capitalize()
    }

    void answer(String answer) {
        this.answer = answer
    }

    void comment(String comment) {
        this.comment = comment
    }
}
