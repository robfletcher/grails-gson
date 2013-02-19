package shopping.list

import groovy.transform.ToString

@ToString
class Artist {

	String name

	static belongsTo = [artist: Artist]

	static constraints = {
		name blank: false, unique: true
	}

}
