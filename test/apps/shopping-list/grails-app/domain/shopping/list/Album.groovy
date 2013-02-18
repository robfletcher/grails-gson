package shopping.list

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
		artist lazy: false, cascade: 'all'
	}
}
