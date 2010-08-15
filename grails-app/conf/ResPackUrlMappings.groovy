class ResPackUrlMappings {
	static mappings = {
        "/css/$packid*.gss"(controller: 'resource', action: 'render') {
            contentType = "text/css"
            encode      = "UTF-8"
            dir         = "css"
        }

        "/js/$packid*.gjs"(controller: 'resource', action: 'render') {
            contentType = "text/javascript"
            encode      = "UTF-8"
            dir         = "js"
        }
  }
}
