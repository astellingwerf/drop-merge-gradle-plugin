package com.opentext.dropmerge.tasks.updatewiki

class SimpleFieldWithComment extends SimpleField {
    void setComment(String comment) {
        setResult 'Comment', comment
    }

    String getComment() {
        getResult 'Comment'
    }

    @Override
    Collection<String> getFieldNames() {
        super.getFieldNames() + ["${fieldName}Comment"]
    }
}
