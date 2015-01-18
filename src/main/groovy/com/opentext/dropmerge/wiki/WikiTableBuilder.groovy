package com.opentext.dropmerge.wiki

import groovy.xml.MarkupBuilder

class WikiTableBuilder {
    private final MarkupBuilder markupBuilder
    private List<String> headers = []
    private List<Map<String, Object>> rows = []

    WikiTableBuilder() {
        this(new MarkupBuilder())
    }

    WikiTableBuilder(MarkupBuilder markupBuilder) {
        this.markupBuilder = markupBuilder
    }

    WikiTableBuilder(Writer writer) {
        this.markupBuilder = new MarkupBuilder(writer)
    }

    WikiTableBuilder(IndentPrinter writer) {
        this.markupBuilder = new MarkupBuilder(writer)
    }

    void setHeaders(List<String> headers) {
        this.headers = headers
    }

    void addRow(Map<String, Object> values) {
        headers.addAll values.keySet().findAll { !headers.contains(it) }
        rows.add(values)
    }

    void addRow(List<Object> values) {
        Map<String, String> m = new HashMap<>()
        values.eachWithIndex { Object entry, int i ->
            m[headers[i]] = entry
        }
        rows.add(m)
    }

    WikiTableBuilder leftShift(Map<String, Object> values) {
        addRow values
        return this
    }

    WikiTableBuilder leftShift(List<Object> values) {
        addRow values
        return this
    }

    public void process() {
        if (!headers.isEmpty()) {
            markupBuilder.table(class: 'confluenceTable') {
                tbody {
                    tr {
                        headers.each { header ->
                            th(class: 'confluenceTh', header)
                        }
                    }
                    rows.each { map ->
                        tr {
                            headers.each { header ->
                                td(class: 'confluenceTd') {
                                    if (map[header] instanceof Closure)
                                        delegate.with map[header]
                                    else if (map[header])
                                        mkp.yield map[header]
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    static MarkupBuilder newMarkupBuilder(Writer writer) {
        return new MarkupBuilder(new IndentPrinter(writer, '', false))
    }

    static String withHtml(Closure<String> c) {

        Writer writer = new StringWriter()
        MarkupBuilder htmlBuilder = newMarkupBuilder(writer)
        c(htmlBuilder)
        return writer.toString()
    }

    static String table(Closure<Void> c) {
        return withHtml { MarkupBuilder html ->
            WikiTableBuilder table = new WikiTableBuilder(html)
            c(table)
            table.process()
        }
    }
}
