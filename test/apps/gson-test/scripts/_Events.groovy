eventAllTestsStart = {
	def specTestTypeClass = loadSpecTestTypeClass()
	functionalTests << specTestTypeClass.newInstance('spock', 'functional')
}