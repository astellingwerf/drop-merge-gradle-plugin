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

    void answer(String answer, String comment) {
        this.answer(answer)
        this.comment(comment)
    }

    void comment(String comment) {
        this.comment = comment
    }

    void no(String comment) { this.answer('No', comment) }

    void yes(String comment) { this.answer('Yes', comment) }

    def not(Object c) {
        return c
    }

    Object getApplicable() { captureComment('Not applicable') }

    Object getTested() { captureComment('Not tested') }

    Object captureComment(String answer) {
        new Object() {
            @Override
            Object getProperty(String comment) { this.answer(answer, comment) }
        }
    }
}
