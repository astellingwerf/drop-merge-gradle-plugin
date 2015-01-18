package com.opentext.dropmerge.tasks.updatewiki

class SimpleFieldWithComment extends SimpleField {
    void setComment(String comment) {
        setResult 'Comment', comment
    }

    String getComment() {
        getResult 'Comment'
    }
}
