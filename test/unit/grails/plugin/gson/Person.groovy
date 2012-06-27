package grails.plugin.gson

import grails.persistence.*

@Entity
class Person {

	String name
    int age
	Address address

	static embedded = ['address']

    static constraints = {
    	name blank: false
        age min: 0
    	address nullable: true
    }

}

class Address {

    int number
    String street

}