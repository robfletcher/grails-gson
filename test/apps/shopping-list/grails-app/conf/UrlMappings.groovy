class UrlMappings {

	static mappings = {
		"/items"(controller: 'item', action: 'list')
		"/item/$id?"(controller: 'item') {
			action = [GET: 'show', PUT: 'update', DELETE: 'delete', POST: 'save']
		}

		"404" controller: 'errors', action: 'notFound'
		"500" controller: 'errors', action: 'serverError'
	}
}
