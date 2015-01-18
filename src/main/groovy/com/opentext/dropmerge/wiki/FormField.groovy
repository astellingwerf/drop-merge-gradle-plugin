package com.opentext.dropmerge.wiki

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper;

public class FormField {
    def item

    private FormField(def item) {
        this.item = item
    }

    public def getRawItem() { item }

    public String getName() { item['@sd-name'].text() }

    public String getParent() { item['@sd-parent'].text() }

    public String getType() { item['@sd-type'].text() }

    public String getParams() { item['@sd-params'].text().trim() }

    public String getContent() { getAndFormatContent(item, type) }

    private static String getAndFormatContent(def node, String type) {
        if (type == 'date' || type == 'number') {
            return node.text()
        } else if (type == 'list') {
            def selectedOptions = node['@sd-selectedoptions'].text().trim()
            def optionsValue = new String(selectedOptions.decodeBase64())
            def jsonResult = new JsonBuilder()
            if (optionsValue)
                jsonResult(new JsonSlurper().parseText(optionsValue)[0].value)
            else {
                jsonResult()
            }
            return jsonResult.toString()
        } else if (type == 'richtext') {
            def value = node.text()
            value = new String(value.decodeBase64())
            return value;
        } else
            return "Don't know $type"
    }


}