package shopping.list

class Artist {

	String name

	static belongsTo = [Artist]

	static constraints = {
		name blank: false, unique: true
	}

}
