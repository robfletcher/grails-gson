[![Build Status](https://travis-ci.org/robfletcher/grails-gson.png)][6]

This plugin provides alternate JSON (de)serialization for Grails using Google's [Gson][1] library.

## Rationale

Grails' JSON deserialization has some limitations. Specifically it doesn't work with nested object graphs. This means
you can't bind a JSON data structure to a GORM domain class and have it populate associations, embedded properties, etc.
 There is a [JIRA][2] open for this issue but since it's easy to provide an alternative with _Gson_ I thought a plugin
 was worthwhile.

## Installation

Add `compile ':gson:1.0-SNAPSHOT'` to `grails-app/conf/BuildConfig.groovy`.

## Usage

### Using Grails converters

The plugin provides a Grails converter implementation so that you can replace usage of the existing
`grails.converters.JSON` class with `grails.plugin.gson.GSON`. For example:

``` groovy
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
```

### Using Gson directly

Alternatively, the plugin provides a [`GsonBuilder`][7] factory bean that you can inject into your components. This is
pre-configured to register type handlers for domain classes so you don't need to worry about doing so unless you need to
override specific behaviour.

``` groovy
class PersonController {
	def gsonBuilder

	def list() {
		def gson = gsonBuilder.create()
		def personInstances = Person.list(params)
		render contentType: 'application/json', text: gson.toJson(personInstances)
	}

	def save() {
		def gson = gsonBuilder.create()
		def personInstance = gson.fromJson(request.reader, Person)
		if (personInstance.save()) {
			// ... etc.
	}

	def update() {
		def gson = gsonBuilder.create()
		// because the incoming JSON contains an id this will read the Person
		// from the database and update it!
		def personInstance = gson.fromJson(request.reader, Person)
	}
}
```

## Serialization

The plugin will automatically resolve any _Hibernate_ proxies it encounters when serializing an object graph to JSON.

If an object graph contains bi-directional relationships they will only be traversed once but in either direction. For
example if you have the following domain classes:

``` groovy
class Artist {
	String name
	static hasMany = [albums: Album]
}

class Album {
	String title
	static belongsTo = [artist: Artist]
}
```

Instances of `Album` will get serialized to JSON as:

``` json
{
	"id": 2,
	"title": "The Rise and Fall of Ziggy Stardust and the Spiders From Mars",
	"artist": {
		"id": 1,
		"name": "David Bowie"
	}
}
```

And instances of `Artist` will get serialized to JSON as:

``` json
{
	"id": 1,
	"name": "David Bowie",
	"albums": [
		{ "id": 1, "title": "Hunky Dory" },
		{ "id": 2, "title": "The Rise and Fall of Ziggy Stardust and the Spiders From Mars" },
		{ "id": 3, "title": "Low" }
	]
}
```

## Deserialization

The plugin registers a [`JsonDeserializer`][8] that handles conversion of JSON to Grails domain objects. It will handle deserialization at any level of a JSON object graph so embedded objects, relationships and persistent collections can all be modified when binding to the top level domain object instance.

If a JSON object contains an `id` property then it will use GORM to retrieve an existing instance, otherwise it creates a new one.

Any other properties of the JSON object are bound to the domain instance. The deserializer respects the [`bindable`][9] constraint so any properties that are blacklisted from binding are ignored.

### Deserialization examples

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

## Registering additional type adapters

The `gsonBuilder` factory bean provided by the plugin will automatically register any Spring beans that implement the
[`TypeAdapterFactory`][3] interface.

### Example

To register support for serializing and deserializing `org.joda.time.LocalDate` properties you would define a
[`TypeAdapter`][4] implementation:

``` groovy
class LocalDateAdapter extends TypeAdapter<LocalDate> {

	private final formatter = ISODateTimeFormat.date()

	void write(JsonWriter jsonWriter, LocalDateTime t) {
		jsonWriter.value(t.toString(formatter))
	}

	LocalDateTime read(JsonReader jsonReader) {
		formatter.parseLocalDate(jsonReader.nextString())
	}
}
```

Then create a `TypeAdapterFactory`:

``` groovy
class LocalDateAdapterFactory implements TypeAdapterFactory {
	TypeAdapter create(Gson gson, TypeToken type) {
		type.rawType == LocalDate ? new LocalDateAdapter() : null
	}
}
```

Finally register the `TypeAdapterFactory` in `grails-app/conf/spring/resources.groovy`:

``` groovy
beans {
	localDateAdapterFactory(LocalDateAdapterFactory)
}
```

The plugin will then automatically use it.

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

See [the Grails documentation on the `cascade` mapping][5]
for more information.

### Circular references

Gson does not support serializing object graphs with circular references and a `StackOverflowException` will be thrown
if you try. The plugin protects against circular references caused by bi-directional relationships in GORM domain
classes but any other circular reference is likely to cause a problem when serialized. If your domain model contains
such relationships you will need to register additional `TypeAdapter` implementations for the classes involved.

[1]:http://code.google.com/p/google-gson/
[2]:http://jira.grails.org/browse/GRAILS-9220
[3]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapterFactory.html
[4]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapter.html
[5]:http://grails.org/doc/latest/ref/Database%20Mapping/cascade.html
[6]:https://travis-ci.org/robfletcher/grails-gson
[7]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html
[8]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/JsonDeserializer.html
[9]:http://grails.org/doc/latest/ref/Constraints/bindable.html
