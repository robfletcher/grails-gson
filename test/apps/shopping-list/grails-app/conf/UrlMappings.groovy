class UrlMappings {

	static mappings = {
		"/items"(controller: 'item', action: 'list')
		"/item/$id?"(controller: 'item') {
			action = [GET: 'show', PUT: 'update', DELETE: 'delete', POST: 'save']
		}

		"/" view: '/index'
		"500" view: '/error'
	}
}
