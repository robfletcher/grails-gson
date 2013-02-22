class UrlMappings {

	static mappings = {
		"/${controller}s"(action: 'list')
		"/album/$id?"(resource: 'album')
		"/publication/$id?"(resource: 'publication')

		"404" controller: 'errors', action: 'notFound'
		"500" controller: 'errors', action: 'serverError'
	}
}
