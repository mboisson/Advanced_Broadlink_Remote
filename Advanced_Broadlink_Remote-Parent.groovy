/*
 *  Advanced Broadlink Remote Parent App
 *  Project URL: https://github.com/mboisson/Advanced_Broadlink_Remote
 *  Copyright 2024 Maxime Boissonneault
 *
 *  This app requires it's child app and device driver to function, please go to the project page for more information.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * 
 */

/*
Heavily inspired by Advanced vThermostat Manager by Nelson Clark.
*/

definition(
	name: "Advanced Broadlink Remote Manager",
	namespace: "mboisson",
	author: "Maxime Boissonneault",
	description: "Use Broadlink remotes by tomw combined with power sensors to create virtual devices that turn dumb appliances into quasi smart ones.",
	category: "Green Living",
	iconUrl: "https://raw.githubusercontent.com/mboisson/Hubitat/main/Apps/Advanced_Broadlink_Remote/Advanced_Broadlink_Remote-logo-small.png",
	iconX2Url: "https://raw.githubusercontent.com/mboisson/Hubitat/main/Apps/Advanced_Broadlink_Remote/Advanced_Broadlink_Remote-logo.png",
	importUrl: "https://raw.githubusercontent.com/mboisson/Hubitat/main/Apps/Advanced_Broadlink_Remote/Advanced_Broadlink_Remote-Parent.groovy",
	singleInstance: true
)

preferences {
	page(name: "Install", title: "Advanced Broadlink Remote Manager", install: true, uninstall: true) {
		section("Devices") {
		}
		section {
			app(name: "remotes", appName: "Advanced Broadlink Remote Child", namespace: "mboisson", title: "Add Advanced Broadlink Remote", multiple: true)
			app(name: "tv_remotes", appName: "Advanced Broadlink TV Remote Child", namespace: "mboisson", title: "Add Advanced Broadlink TV Remote", multiple: true)
        }
	}
}

def installed() {
	log.debug "Installed"
	initialize()
}

def updated() {
	log.debug "Updated"
	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "Initializing; there are ${childApps.size()} child apps installed"
	childApps.each {child -> 
		log.debug "  child app: ${child.label}"
	}
}
