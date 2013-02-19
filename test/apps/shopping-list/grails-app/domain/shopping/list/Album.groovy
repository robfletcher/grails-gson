package shopping.list

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
		artist lazy: false, // https://github.com/robfletcher/grails-gson/issues/14
				cascade: 'all' // https://github.com/robfletcher/grails-gson/issues/15
	}
}
