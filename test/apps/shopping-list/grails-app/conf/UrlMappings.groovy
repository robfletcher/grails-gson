class UrlMappings {

	static mappings = {
		"/albums"(controller: 'album', action: 'list')
		"/album/$id?"(controller: 'album') {
			action = [GET: 'show', PUT: 'update', DELETE: 'delete', POST: 'save']
		}

		"404" controller: 'errors', action: 'notFound'
		"500" controller: 'errors', action: 'serverError'
	}
}
