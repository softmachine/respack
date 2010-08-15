class RespackGrailsPlugin {
    def version = "0.2"
    def grailsVersion = "1.3.4 > *"
    def dependsOn = [:]
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/views/index.gsp"
    ]

    def author = "Mike P. Kuhl"
    def authorEmail = "mkuhl@softmachine.at"
    def title = "soft::machine resource packager plugin"
    def description = '''\\
render javascript, css, and other application resources as gsp and allows bundeling of theese resources for more
efficient page loading
'''

    def documentation = "http://softmachine.at/dev/respack"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
