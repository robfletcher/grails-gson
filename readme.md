This plugin provides alternate JSON (de)serialization for Grails using Google's [Gson][gson] library.

## Rationale

Grails' JSON deserialization has some limitations. Specifically it doesn't work with nested object graphs. This means you can't bind a JSON data structure to a GORM domain class and have it populate associations, embedded properties, etc. There is a [JIRA][grails-9220] open for this issue but since it's easy to provide an alternative with _Gson_ I thought a plugin was worthwhile.

## Usage

### Using Gson directly

The plugin provides a _gsonFactory_ bean that you can inject into your components. This is pre-configured to register type handlers for domain classes so you don't need to worry about doing so unless you need to override specific behaviour.

	class PersonController {
		def gsonFactory

		def list() {
			def gson = gsonFactory.createGson()
			def personInstances = Person.list(params)
			render contentType: 'application/json', text: gson.toJson(personInstances)
		}

		def save() {
			def gson = gsonFactory.createGson()
			def personInstance = gson.fromJson(request.reader, Person)
			if (personInstance.save()) {
				// ... etc.
		}

		def update() {
			def gson = gsonFactory.createGson()
			// because the incoming JSON contains an id this will read the Person
			// from the database and update it!
			def personInstance = gson.fromJson(request.reader, Person)
		}
	}

This method is convenient if you need to support additional data types. You can register type handlers with the _gsonFactory_ bean.

### Using Grails converters

The plugin also provides a Grails converter implementation so that you can swap out usage of the existing `grails.converters.JSON` class with `grails.plugin.gson.JSON`. For example:

	import grails.plugin.gson.JSON

	class PersonController {
		def list() {
			render Person.list(params) as JSON
		}

		def save() {
			def personInstance = new Person(request.JSON)
			// ... etc.
		}

		def update() {
			def personInstance = Person.get(params.id)
			personInstance.properties = request.JSON
			// ... etc.
		}
	}

This method is useful if you want to continue using Grails conventions in your code.

## Deserialization examples

Let's say you have a domain classes _Child_ and _Pet_ like this:

``` groovy
class Child {
	String name
	int age
	static hasMany = [pets: Pet]
}

class Pet {
	String name
	String species
}
```

This can be deserialized in a number of ways.

### To create a new _Child_ instance with associated _Pet_ instances

``` json
{
	"name": "Alex",
	"age": 3,
	"pets": [
		{"name": "Goldie", "species": "Goldfish"},
		{"name": "Dottie", "species": "Goldfish"}
	]
}
```

### To bind new _Pet_ instances to an existing _Child_

``` json
{
	"id": 1,
	"pets": [
		{"name": "Goldie", "species": "Goldfish"},
		{"name": "Dottie", "species": "Goldfish"}
	]
}
```

### To bind existing _Pet_ instances to a new _Child_

``` json
{
	"name": "Alex",
	"age": 3,
	"pets": [
		{"id": 1},
		{"id": 2}
	]
}
```

### To update the _name_ of existing _Pet_ instances without changing their _species_

``` json
{
	"id": 1,
	"pets": [
		{"id": 1, "name": "Goldie"},
		{"id": 2, "name": "Dottie"}
	]
}
```

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