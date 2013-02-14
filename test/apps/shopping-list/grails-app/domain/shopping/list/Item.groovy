package shopping.list

class Item {

	String description
	Integer quantity
	String unit

    static constraints = {
		description blank: false, unique: true
		quantity min: 1
		unit nullable: true
    }
}
