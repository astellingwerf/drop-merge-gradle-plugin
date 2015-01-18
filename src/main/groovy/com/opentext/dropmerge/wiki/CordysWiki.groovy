package com.opentext.dropmerge.wiki

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.Memoized
import groovyx.net.http.HTTPBuilder
import org.apache.http.cookie.Cookie
import org.apache.http.impl.cookie.BasicClientCookie

import static groovyx.net.http.ContentType.URLENC

class CordysWiki {
    private HTTPBuilder wikiHttp = new HTTPBuilder('https://wiki.cordys.com')

    public void authenticate(String wikiUserName, String wikiPassword) {
        wikiHttp.client.cookieStore.addCookie(getAuthenticationCookie(wikiUserName, wikiPassword))
    }

    private Cookie getAuthenticationCookie(String wikiUserName, String wikiPassword) {
        String tokenKey = null

        def cams = new HTTPBuilder('https://www.opentext.com')

        cams.handler.'500' = { resp, reader ->
            throw new IllegalArgumentException("Error occured during authentication: ${resp.statusLine}")
        }

        cams.post(path: '/cams/login',
                contentType: URLENC,
                body: [cams_cb_username: wikiUserName, cams_cb_password: wikiPassword, cams_security_domain: 'system', cams_login_config: 'http', cams_original_url: 'https://wiki.cordys.com/']) { resp, reader ->
            assert resp.statusLine.statusCode == 302
            def header = resp.headers.find { h -> return h.name.equals('Set-Cookie') && h.value.startsWith('CAMS_SID_OT_SYSTEM=') }
            if((tokenKey = header?.value) == null)
                throw new IllegalArgumentException('The wiki username or password is incorrect')
        }

        String[] kvp = tokenKey.split('; ')[0].split('=')
        def authCookie = new BasicClientCookie(kvp[0], kvp[1])
        authCookie.domain = '.cordys.com'
        authCookie.path = '/'
        authCookie.secure = true
        return authCookie
    }

    public void eachDropMergeField(String pageID, Closure<?> closure) {
        wikiHttp.get(path: '/pages/editscaffold.action', query: [pageId: pageID]) { resp, reader ->
            getEditForm(reader).'**'
                    .findAll { isFormField(it) }
                    .collect { new FormField(it) }
                    .each(closure)
        }
    }

    @Memoized
    public Map<String, FormField> getDropMergeFields(String pageID) {
        def result = null
        wikiHttp.get(path: '/pages/editscaffold.action', query: [pageId: pageID]) { resp, reader ->
            result = getEditForm(reader).'**'
                    .findAll { isFormField(it) }
                    .collect { new FormField(it) }
                    .collectEntries { [(it.name): it] }
        }
        return result
    }

    public void updateDropMergePage(String pageID, Map<String, String> data, boolean postToRealServer) {
        def updateQuery = [
                pageId         : pageID,
                entityId       : pageID,
                mode           : 'edit',
                originalContent: '',
                wysiwygContent : '',
                decorator      : 'none',
                formMode       : 'forms',
                contentType    : 'page',
                formName       : 'scaffold-form',
                versionComment : 'Automatically updated by script.',
                notifyWatchers : 'false']


        wikiHttp.get(path: '/pages/editscaffold.action', query: [pageId: pageID]) { resp, reader ->
            def form = getEditForm(reader)

            ['originalVersion', 'conflictingVersion', 'parentPageString', 'newSpaceKey'].each {
                updateQuery[it] = getValueOfInputFieldById(form, it)
            }
            ['content-title': 'title', 'newSpaceKey': 'spaceKey'].each { sourceFieldName, targetParam ->
                updateQuery[targetParam] = getValueOfInputFieldById(form, sourceFieldName)
            }

            def json = new JsonBuilder()

            json {
                form.'**'
                        .findAll { isFormField(it) }
                        .collect { new FormField(it) }
                        .each { FormField formField ->

                    String contentValue = data.containsKey(formField.name) ? data[formField.name] : formField.content
                    "${formField.name}" {
                        name(formField.name)
                        children {}
                        parent(formField.parent)
                        type(formField.type)
                        params(formField.params)
                        content(contentValue)
                    }
                }
            }

            updateQuery['jsonContent'] = json.toString()
        }

        def destinationHttp = wikiHttp
        if (!postToRealServer) {
            destinationHttp = new HTTPBuilder('http://localhost')
            destinationHttp.setProxy('localhost', 8888, 'http')
        }

        destinationHttp.post(path: '/pages/doeditscaffold.action', contentType: URLENC, body: updateQuery) { resp ->
            println "HTTP response status: ${resp.statusLine}"
        }
    }

    private static boolean isFormField(def it) {
        it.name().equalsIgnoreCase('span') && it['@sd-name'] && it['@sd-name'].text()
    }

    private static getEditForm(def reader) {
        findByTagAndId(reader, 'form', 'editpageform')
    }

    private static def findByTagAndId(def node, String type, String id) {
        node.'**'.find { it.@id == id && it.name().equalsIgnoreCase(type) }
    }

    private static def getValueOfInputFieldById(def node, String id) {
        findByTagAndId(node, 'input', id).@value
    }

    public static def getJsonForOptions(item) {
        return new JsonSlurper().parseText(new String(item['@sd-options'].text().trim().decodeBase64()))
    }

    public static String selectOption(def item, String option) {
        def options = getJsonForOptions(item)
        List<String> optionsNames = options.collect { it.name }
        if (!optionsNames.contains(option))
            throw new IllegalArgumentException("Option should be one of $optionsNames")

        def jsonResult = new JsonBuilder()
        jsonResult(options.find { it.name == option }.value)
        return jsonResult.toString()
    }

}
