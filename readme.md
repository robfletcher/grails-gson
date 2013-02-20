[![Build Status](https://travis-ci.org/robfletcher/grails-gson.png)](https://travis-ci.org/robfletcher/grails-gson)

This plugin provides alternate JSON (de)serialization for Grails using Google's [Gson][gson] library.

## Rationale

Grails' JSON deserialization has some limitations. Specifically it doesn't work with nested object graphs. This means you can't bind a JSON data structure to a GORM domain class and have it populate associations, embedded properties, etc. There is a [JIRA][grails-9220] open for this issue but since it's easy to provide an alternative with _Gson_ I thought a plugin was worthwhile.

## Usage

### Using Grails converters

The plugin provides a Grails converter implementation so that you can swap out usage of the existing `grails.converters.JSON` class with `grails.plugin.gson.GSON`. For example:

	import grails.plugin.gson.GSON

	class PersonController {
		def list() {
			render Person.list(params) as GSON
		}

		def save() {
			def personInstance = new Person(request.GSON)
			// ... etc.
		}

		def update() {
			def personInstance = Person.get(params.id)
			personInstance.properties = request.GSON
			// ... etc.
		}
	}

This method is useful if you want to continue using Grails conventions in your code.

### Using Gson directly

Alternatively, the plugin provides a _gsonFactory_ bean that you can inject into your components. This is pre-configured to register type handlers for domain classes so you don't need to worry about doing so unless you need to override specific behaviour.

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
	static belongsTo = [child: Child]
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

## Gotchas

When trying to bind an entire object graph you need to be mindful of the way GORM cascades persistence changes.

### Cascading updates

Even though you can bind nested domain relationships there need to be cascade rules in place so that they will save.

In the examples above the _Pet_ domain class must declare that it `belongsTo` _Child_ (or _Child_ must declare that
updates cascade to `pets`). Otherwise the data will bind but when you save the _Child_ instance the changes to any
nested _Pet_ instances will not be persisted.

### Cascading saves

Likewise if you are trying to create an entire object graph at once the correct cascade rules need to be present.

If _Pet_ declares `belongsTo = [child: Child]` everything should work as Grails will apply cascade _all_ by default.
However if _Pet_ declares `belongsTo = Child` then _Child_ needs to override the default cascade _save-update_ so that
new _Pet_ instances are created properly.

See [the Grails documentation on the `cascade` mapping](http://grails.org/doc/latest/ref/Database%20Mapping/cascade.html)
for more information.

[gson]:http://code.google.com/p/google-gson/
[grails-9220]:http://jira.grails.org/browse/GRAILS-9220