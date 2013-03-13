includeTargets << grailsScript('_GrailsInit')

target(main: 'Installs the REST controller template') {
	depends checkVersion, parseArguments

	event 'StatusUpdate', ["Installing Templates from $gsonPluginDir..."]

	targetDir = "$basedir/src/templates"
	overwrite = false
	// only if template dir already exists in, ask to overwrite templates
	if (new File(targetDir).exists()) {
		if (!isInteractive || confirmInput('Overwrite existing templates?', 'overwrite.templates')) {
			overwrite = true
		}
	} else {
		ant.mkdir dir: targetDir
	}

	ant.copy(todir: "$targetDir/scaffolding", overwrite: overwrite) {
		fileset dir: "$gsonPluginDir/src/templates/scaffolding"
	}

	event 'StatusFinal', ['Templates installed successfully']
}

setDefaultTarget main
