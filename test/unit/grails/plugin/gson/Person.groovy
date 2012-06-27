package grails.plugin.gson

import grails.persistence.*

@Entity
class Person {

	String name
    int age
	Address address
    Collection<Pet> pets

	static embedded = ['address']
    static hasMany = [pets: Pet]

    static constraints = {
    	name blank: false
        age min: 0
    	address nullable: true
    }

}

@Entity
class Pet {

    String name
    String species

    static constraints = {
        name blank: false
    }

}

class Address {

    int number
    String street

}