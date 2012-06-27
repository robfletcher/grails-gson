This plugin provides alternate JSON (de)serialization for Grails using Google's [Gson][gson] library.

## Rationale

Grails' JSON deserialization has some limitations. Specifically it doesn't work with nested object graphs. This means you can't bind a JSON data structure to a GORM domain class and have it populate associations, embedded properties, etc. There is a [JIRA][grails-9220] open for this issue but since it's easy to provide an alternative with _Gson_ I thought a plugin was worthwhile.

[gson]:http://code.google.com/p/google-gson/
[grails-9220]:http://jira.grails.org/browse/GRAILS-9220