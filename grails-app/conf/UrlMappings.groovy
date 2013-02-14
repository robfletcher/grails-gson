class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"404" controller: 'errors', action: 'notFound'
		"500" controller: 'errors', action: 'serverError'
	}
}
