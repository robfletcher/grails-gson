class UrlMappings {

	static mappings = {
		"/${controller}s"(action: 'list')
		"/album/$id?"(resource: 'album')
		"/artist/$id?"(resource: 'artist')
		"/publication/$id?"(resource: 'publication')

		"404" controller: 'errors', action: 'notFound'
		"500" controller: 'errors', action: 'serverError'
	}
}
