package org.jenkinsci.images

import groovy.transform.Memoized

class IconCSS {

    @Memoized
    public static String getStyle() {
        StringBuilder sb = new StringBuilder('.jenkinsJobStatus{background-repeat: no-repeat; padding-left: 19px; padding-top: 3px; width: 16px; height: 16px;}').append System.lineSeparator()

        def extraStyles = [grey: ['aborted', 'disabled'], grey_anime: ['aborted_anime', 'disabled_anime']]
		def images = ['blue','yellow','red','grey'].collectMany { ["${it}.png", "${it}_anime.gif"] }
		images.each { String imageFileName ->
        	sb.append '.jenkinsJobStatus_'
			sb.append imageFileName[0..-5]
			sb.append ' '
			if (extraStyles.containsKey(imageFileName[0..-5])) {
				extraStyles[imageFileName[0..-5]].each { String extraStyle ->
					sb.append ', .jenkinsJobStatus_'
					sb.append extraStyle
					sb.append ' '
				}
			}
			sb.append '{background-image: url(data:image/' + imageFileName[-3..-1] + ';base64,'
			InputStream resource = IconCSS.class.getClassLoader().getResourceAsStream(IconCSS.class.package.name.replace('.', '/')+'/'+imageFileName)
			sb.append resource.bytes.encodeBase64().toString()
			sb.append ')}'
			sb.append System.lineSeparator()
        }

        return sb.toString()
    }
}
