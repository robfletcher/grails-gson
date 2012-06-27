This plugin provides alternate JSON (de)serialization for Grails using Google's [Gson][gson] library.

## Rationale

Grails' JSON deserialization has some limitations. Specifically it doesn't work with nested object graphs. This means you can't bind a JSON data structure to a GORM domain class and have it populate associations, embedded properties, etc. There is a [JIRA][grails-9220] open for this issue but since it's easy to provide an alternative with _Gson_ I thought a plugin was worthwhile.

## Deserialization examples

Let's say you have a domain classes _Child_ and _Pet_ like this:

	class Child {
		String name
		int age
		static hasMany = [pets: Pet]
	}

	class Pet {
		String name
		String species
	}

This can be deserialized in a number of ways.

### To create a new _Child_ instance with associated _Pet_ instances

	{
		"name": "Alex",
		"age": 3,
		"pets": [
			{"name": "Goldie", "species": "Goldfish"},
			{"name": "Dottie", "species": "Goldfish"}
		]
	}

### To bind new _Pet_ instances to an existing _Child_

	{
		"id": 1,
		"pets": [
			{"name": "Goldie", "species": "Goldfish"},
			{"name": "Dottie", "species": "Goldfish"}
		]
	}

### To bind existing _Pet_ instances to a new _Child_

	{
		"name": "Alex",
		"age": 3,
		"pets": [
			{"id": 1},
			{"id": 2}
		]
	}

### To update the _name_ of existing _Pet_ instances without changing their _species_

	{
		"id": 1,
		"pets": [
			{"id": 1, "name": "Goldie"},
			{"id": 2, "name": "Dottie"}
		]
	}

## Compatibility

The plugin's Gson deserializer works with:

- domain classes
- domain associations
- _Set_, _List_ and _Map_ associations
- embedded properties
- collections of basic types
- arbitrary depth object graphs

[gson]:http://code.google.com/p/google-gson/
[grails-9220]:http://jira.grails.org/browse/GRAILS-9220