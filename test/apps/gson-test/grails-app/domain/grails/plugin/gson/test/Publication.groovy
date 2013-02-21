package grails.plugin.gson.test

class Publication {
	String isbn
	String title
	static mapping = {
		id name: 'isbn', generator: 'assigned'
	}
}