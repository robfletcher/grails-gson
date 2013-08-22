# Grails Gson plugin

[![Build Status](https://travis-ci.org/robfletcher/grails-gson.png)][6]

This plugin provides alternate JSON (de)serialization for Grails using Google's [Gson][1] library.

## Rationale

Grails' JSON deserialization has some limitations. Specifically it doesn't work with nested object graphs. This means you can't bind a JSON data structure to a GORM domain class and have it populate associations, embedded properties, etc.

There is a [JIRA][2] open for this issue but since it's easy to provide an alternative with _Gson_ I thought a plugin was worthwhile.

## Installation

Add `compile 'org.grails.plugins:gson:1.1.4'` to `grails-app/conf/BuildConfig.groovy`.

## Usage

### Using Grails converters

The plugin provides a Grails converter implementation so that you can replace usage of the existing `grails.converters.JSON` class with `grails.plugin.gson.converters.GSON`. For example:

``` groovy
import grails.plugin.gson.converters.GSON

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

The plugin provides a [`GsonBuilder`][7] factory bean that you can inject into your components. This is pre-configured to register type handlers for domain classes so you don't need to worry about doing so unless you need to override specific behaviour.

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

By default the plugin will automatically serialize any _Hibernate_ proxies it encounters when serializing an object graph to JSON, resolving any uninitialized proxies along the way. This means by default you get a full, deep object graph at the potential cost of additional SQL queries. There are two config flags to control this behavior in your _Config.groovy_. If you set `grails.converters.gson.resolveProxies` to `false` then only initialized proxies are serialized – therefore no additional queries are performed. If you set `grails.converters.gson.serializeProxies` to `false` then no proxies are serialized at all meaning your JSON will only contain a shallow object graph.

If an object graph contains bi-directional relationships they will only be traversed once (but in either direction).

For example if you have the following domain classes:

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

The deserializer is pre-configured to handle:

- domain classes
- domain associations
- _Set_, _List_ and _Map_ associations
- embedded properties
- collections of basic types
- arbitrary depth object graphs

If a JSON object contains an `id` property then it will use GORM to retrieve an existing instance, otherwise it creates a new one.

The deserializer respects the [`bindable`][9] constraint so any properties that are blacklisted from binding are ignored. Any JSON properties that do not correspond to persistent properties on the domain class are ignored. Any other properties of the JSON object are bound to the domain instance.

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

The `gsonBuilder` factory bean provided by the plugin will automatically register any Spring beans that implement the [`TypeAdapterFactory`][3] interface.

### Example

To register support for serializing and deserializing `org.joda.time.LocalDate` properties you would define a [`TypeAdapter`][4] implementation:

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

See the [Gson documentation on custom serialization and deserialization][11] for more information on how to write `TypeAdapter` implementations.

## Unit test support

The plugin provides a test mixin. Simply add `@TestMixin(GsonUnitTestMixin)` to test or spec classes. The mixin registers beans in the mock application context that are required for the _GSON_ converter class to work properly. It also ensures that binding and rendering works with _@Mock_ domain classes just as it does in a real running application.

In addition the mixin adds:

- a `GSON` property on _HttpServletResponse_ for convenience in making assertions in controller tests.
- a writable `GSON` property on _HttpServletResponse_ that accepts either a _JsonElement_ or a JSON string.

## Scaffolding RESTful controllers

The GSON plugin includes a scaffolding template for RESTful controllers designed to work with Grails' [resource style URL mappings][27]. To install the template run:

    grails install-gson-templates

This will overwrite any existing file in `src/templates/scaffoldng/Controller.groovy`. You can then generate RESTful controllers that use GSON using the normal dynamic or static scaffolding capabilities.

## Gotchas

When trying to bind an entire object graph you need to be mindful of the way GORM cascades persistence changes.

### Cascading updates

Even though you can bind nested domain relationships there need to be cascade rules in place so that they will save.

In the examples above the _Pet_ domain class must declare that it `belongsTo` _Child_ (or _Child_ must declare that updates cascade to `pets`). Otherwise the data will bind but when you save the _Child_ instance the changes to any nested _Pet_ instances will not be persisted.

### Cascading saves

Likewise if you are trying to create an entire object graph at once the correct cascade rules need to be present.

If _Pet_ declares `belongsTo = [child: Child]` everything should work as Grails will apply cascade _all_ by default. However if _Pet_ declares `belongsTo = Child` then _Child_ needs to override the default cascade _save-update_ so that new _Pet_ instances are created properly.

See [the Grails documentation on the `cascade` mapping][5] for more information.

### Circular references

Gson does not support serializing object graphs with circular references and a `StackOverflowException` will be thrown if you try. The plugin protects against circular references caused by bi-directional relationships in GORM domain classes but any other circular reference is likely to cause a problem when serialized. If your domain model contains such relationships you will need to register additional `TypeAdapter` implementations for the classes involved.

### Parameter parsing

In general it is possible to use the Gson plugn alongside Grails' built in JSON support. The only thing the plugin overrides in the parsing of a JSON request body into a parameter map.

This is only done when you set `parseRequest: true` in _URLMappings_ or use a resource style mapping. See [the Grails documentation on REST services][10] for more information.

The plugin's parsing is compatible with that done by the default JSON handler so you should see no difference in the result.

## Configuration

The plugin supports a few configurable options. Where equivalent configuration applies to the standard Grails _JSON_ converter then the same configuration can be used for the _GSON_ converter.

* **grails.converters.gson.serializeProxies** if set to `true` then any Hibernate proxies are traversed when serializing entities to JSON. Defaults to `true`. If set to `false` any _n-to-one_ proxies are serialized as just their identifier and any _n-to-many_ proxies are omitted altogether.

* **grails.converters.gson.resolveProxies** if set to `true` then any Hibernate proxies are initialized when serializing entities to JSON. Defaults to `true`. If set to `false` only proxies that are already initialized get serialized to JSON. This flag has no effect if `grails.converters.gson.serializeProxies` is set to `false` as proxies will not be traversed anyway.

* **grails.converters.gson.pretty.print** if set to `true` then serialization will output pretty-printed JSON. Defaults to `grails.converters.default.pretty.print` or `false`. See [GsonBuilder.setPrettyPrinting][22].

* **grails.converters.gson.domain.include.class** if set to `true` then serialization will include domain class names. Defaults to `grails.converters.domain.include.class` or `false`.

* **grails.converters.gson.domain.include.version** if set to `true` then serialization will include entity version. Defaults to `grails.converters.domain.include.version` or `false`.

* **grails.converters.gson.serializeNulls** if set to `true` then `null` properties are included in serialized JSON, otherwise they are omitted. Defaults to `false`. See [`GsonBuilder.serializeNulls`][13].

* **grails.converters.gson.complexMapKeySerialization** if set to `true` then object map keys are serialized as JSON objects, otherwise their `toString` method is used. Defaults to `false`. See [`GsonBuilder.enableComplexMapKeySerialization`][14].

* **grails.converters.gson.escapeHtmlChars** if set to `true` then HTML characters are escaped in serialized output. Defaults to `true`. See [`GsonBuilder.disableHtmlEscaping`][15].

* **grails.converters.gson.generateNonExecutableJson** if set to `true` then serialized output is prepended with an escape string to prevent execution as JavaScript. Defaults to `false`. See [`GsonBuilder.generateNonExecutableJson`][16].

* **grails.converters.gson.serializeSpecialFloatingPointValues** if set to `true` then serialization will not throw an exception if it encounters a _special_ long value such as _NaN_. Defaults to `false`. See [`GsonBuilder.serializeSpecialFloatingPointValues`][17].

* **grails.converters.gson.longSerializationPolicy** specifies how long values are serialized. Defaults to [`LongSerializationPolicy.DEFAULT`][20]. See [`GsonBuilder.setLongSerializationPolicy`][18].

* **grails.converters.gson.fieldNamingPolicy** specifies how field names are serialized. Defaults to [`FieldNamingPolicy.IDENTITY`][21]. See [`GsonBuilder.setFieldNamingStrategy`][19].

* **grails.converters.gson.datePattern** specifies the pattern used to format `java.util.Date` objects in serialized output. If this is set then `dateStyle` and `timeStyle` are ignored. See [`GsonBuilder.setDateFormat(String)`][23].

* **grails.converters.gson.dateStyle** and **grails.converters.gson.timeStyle** specify the style used to format  `java.util.Date` objects in serialized output. See [`GsonBuilder.setDateFormat(int, int)`][24]. The values should be one of the `int` constants - `SHORT`, `MEDIUM`, `LONG` or `FULL` - from [`java.text.DateFormat`][25]. Note that Gson does not have a way to specify a _locale_ for the format so [`Locale.US`][26] is always used. For more control over the format use _grails.converters.gson.datePattern_ or register a custom `TypeAdapterFactory`.

## Version history

### [1.1.4](https://github.com/robfletcher/grails-gson/issues?milestone=7)

* Fixes a problem in unit tests with `request.GSON = x` where `x` is anything other than a `String`.

### [1.1.3](https://github.com/robfletcher/grails-gson/issues?milestone=6)

* Fixes a bug where the plugin breaks `domainClass.properties = x` where `x` is anything other than a `JsonObject`.

### [1.1.2](https://github.com/robfletcher/grails-gson/issues?milestone=5)

* Adds `GsonUnitTestMixin` for unit test support.

### [1.1.1](https://github.com/robfletcher/grails-gson/issues?milestone=4)

* Fixes a compilation problem with scaffolded controllers that use the RESTful controller template

### [1.1](https://github.com/robfletcher/grails-gson/issues?milestone=3)

* Introduces various configuration options
* Adds RESTful controller template

### [1.0.1](https://github.com/robfletcher/grails-gson/issues?milestone=2)

Bugfix release.

* Fixes deserialization of bi-directional relationships so tbat the domain instances can be save successfully.
* Ignores unknown properties in JSON rather than throwing an exception (contributed by [@gavinhogan][12]).

### [1.0](https://github.com/robfletcher/grails-gson/issues?milestone=1)

Initial release.

[1]:http://code.google.com/p/google-gson/
[2]:http://jira.grails.org/browse/GRAILS-9220
[3]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapterFactory.html
[4]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapter.html
[5]:http://grails.org/doc/latest/ref/Database%20Mapping/cascade.html
[6]:https://travis-ci.org/robfletcher/grails-gson
[7]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html
[8]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/JsonDeserializer.html
[9]:http://grails.org/doc/latest/ref/Constraints/bindable.html
[10]:http://grails.org/doc/latest/guide/webServices.html#REST
[11]:https://sites.google.com/site/gson/gson-user-guide#TOC-Custom-Serialization-and-Deserialization
[12]:https://github.com/gavinhogan
[13]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#serializeNulls()
[14]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#enableComplexMapKeySerialization()
[15]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#disableHtmlEscaping()
[16]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#generateNonExecutableJson()
[17]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#serializeSpecialFloatingPointValues()
[18]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#setLongSerializationPolicy(com.google.gson.LongSerializationPolicy)
[19]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#setFieldNamingStrategy(com.google.gson.FieldNamingStrategy)
[20]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/LongSerializationPolicy.html#DEFAULT
[21]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/FieldNamingPolicy.html#IDENTITY
[22]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#setPrettyPrinting()
[23]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#setDateFormat(java.lang.String)
[24]:http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html#setDateFormat(int,%20int)
[25]:http://docs.oracle.com/javase/7/docs/api/java/text/DateFormat.html
[26]:http://docs.oracle.com/javase/7/docs/api/java/util/Locale.html#US
[27]:http://grails.org/doc/latest/guide/single.html#REST
