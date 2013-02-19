import grails.plugin.gson.test.*

fixture {

	divineFits Artist, name: 'Divine Fits'

	aThingCalledDivineFits Album,
			artist: divineFits,
			title: 'A Thing Called Divine Fits',
			year: 2012,
			tracks: [
					"My Love Is Real",
					"Flaggin’ A Ride",
					"What Gets You Alone",
					"Would That Not Be Nice",
					"The Salton Sea",
					"Baby Get Worse",
					"Civilian Stripes",
					"For Your Heart",
					"Shivers",
					"Like Ice Cream",
					"Neopolitans"
			]

}