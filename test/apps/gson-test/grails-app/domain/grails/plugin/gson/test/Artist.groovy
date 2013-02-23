package grails.plugin.gson.test

import groovy.transform.ToString

@ToString
class Artist {

	String name

	static belongsTo = Album

	static constraints = {
		name blank: false, unique: true
	}

}
