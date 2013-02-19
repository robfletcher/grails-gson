package grails.plugin.gson.test

import groovy.transform.ToString

@ToString
class Album {

	Artist artist
	String title
	Integer year
	List<String> tracks

	static hasMany = [tracks: String]

	static constraints = {
		artist bindable: true
		title blank: false, unique: true
		year nullable: true
	}

	static mapping = {
		artist cascade: 'all' // https://github.com/robfletcher/grails-gson/issues/15
	}
}
