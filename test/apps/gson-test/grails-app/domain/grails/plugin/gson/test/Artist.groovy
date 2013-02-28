package grails.plugin.gson.test

import groovy.transform.ToString

@ToString(includes = ['name'], includeNames = true)
class Artist {

	String name
	List<Album> albums

	static hasMany = [albums: Album]

	static constraints = {
		name blank: false, unique: true
	}

}
