class UrlMappings {

	static mappings = {
		"/${controller}s"(action: 'list')
		"/$controller/$id?" {
			action = [GET: 'show', PUT: 'update', DELETE: 'delete', POST: 'save']
		}

		"404" controller: 'errors', action: 'notFound'
		"500" controller: 'errors', action: 'serverError'
	}
}
