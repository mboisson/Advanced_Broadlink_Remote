/*
 *  Advance Broadlink TV Remote Child App
 *  Project URL: https://github.com/mboisson/Advanced_Broadlink_Remote
 *  Copyright 2024 Maxime Boissonneault
 *
 *  This app requires it's parent app and device driver to function, please go to the project page for more information.
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

definition(
	name: "Advanced Broadlink TV Remote Child",
	namespace: "mboisson",
	author: "Maxime Boissonneault",
	description: "Use Broadlink remotes by tomw combined with power sensors to create virtual devices that turn dumb TVs into quasi smart ones.",
	category: "Green Living",
	iconUrl: "https://raw.githubusercontent.com/mboisson/Advanced_Broadlink_Remote/main/Advanced_Broadlink_Remote-logo-small.png",
	iconX2Url: "https://raw.githubusercontent.com/mboisson/Advanced_Broadlink_Remote/main/Advanced_Broadlink_Remote-logo.png",
	importUrl: "https://raw.githubusercontent.com/mboisson/Advanced_Broadlink_Remote/main/Advanced_Broadlink_TV_Remote-Child.groovy",
	parent: "mboisson:Advanced Broadlink Remote Manager"
)


preferences {
	page(name: "pageConfig") // Doing it this way elimiates the default app name/mode options.
}


def pageConfig() {
	// Let's just set a few things before starting
	installed = false
    
	if (!state.deviceID) {
        installed = true
    }
        
    // Display all options for a new instance of the Advanced Broadlink Remote
	dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {
		section() {
			label title: "Name of new Advanced Broadlink TV Remote app/device:", required: true
		}
		
		section("Select TV power sensor"){
			input "tv_sensor", "capability.powerMeter", title: "TV power sensor", multiple: false, required: false
		}
		section("Select sound power sensor"){
			input "sound_sensor", "capability.powerMeter", title: "Sound power sensor", multiple: false, required: false
		}

		section("Select remote to use to control TV"){
			input "tv_remote", "capability.actuator", title: "TV remote", multiple: false
		}
		section("Select remote to use to control sound"){
			input "sound_remote", "capability.actuator", title: "Sound remote", multiple: false
		}
        section("Delay between commands") {
			input (name: "delay", type: "number", title: "When multiple commands (comma separated) are to be sent, how long to wait between them (in milliseconds)", required: true, defaultValue: 1000)
		}
		section("Log Settings...") {
			input (name: "logLevel", type: "enum", title: "Live Logging Level: Messages with this level and higher will be logged", options: [[0: 'Disabled'], [1: 'Error'], [2: 'Warning'], [3: 'Info'], [4: 'Debug'], [5: 'Trace']], defaultValue: 3)
			input "logDropLevelTime", "decimal", title: "Drop down to Info Level Minutes", required: true, defaultValue: 5
		}
	}
}


def installed() {
    
	// Set log level as soon as it's installed to start logging what we do ASAP
	int loggingLevel
	if (settings.logLevel) {
		loggingLevel = settings.logLevel.toInteger()
	} else {
		loggingLevel = 3
	}
	
	logger("trace", "Installed Running Broadlink Remote: $app.label")
	
	// Generate a random DeviceID
	state.deviceID = "avtvbr" + Math.abs(new Random().nextInt() % 9999) + 1

	//Create Broadlink Remote device
	def child
	def label = app.getLabel()
	logger("info", "Creating Broadlink TV Remote Child : ${label} with device id: ${state.deviceID}")
	try {
		child = addChildDevice("mboisson", "Advanced Broadlink TV Remote Device", state.deviceID, null, [label: label, name: label, completedSetup: true]) 
	} catch(e) {
		logger("error", "Error adding Broadlink Remote Child ${label}: ${e}") 
	}
	initialize(child)
}


def updated() {
	// Set log level to new value
	int loggingLevel
	if (settings.logLevel) {
		loggingLevel = settings.logLevel.toInteger()
	} else {
		loggingLevel = 3
	}
	
	logger("trace", "Updated Running Broadlink TV Remote: $app.label")

	initialize(getChild())
}


def uninstalled() {
	logger("info", "Child Device " + state.deviceID + " removed") // This never shows in the logs, is it because of the way HE deals with the uninstalled method?
	deleteChildDevice(state.deviceID)
}


//************************************************************
// initialize
//     Set preferences in the associated device and subscribe to the selected sensors and remote device
//     Also set logging preferences
//
// Signature(s)
//     initialize(child)
//
// Parameters
//     child : deviceWrapper
//
// Returns
//     None
//
//************************************************************
def initialize(child) {
	logger("trace", "Initialize Running Broadlink Remote: $app.label")

	// First we need tu unsubscribe and unschedule any previous settings we had
	unsubscribe()
	unschedule()

	// Recheck Log level in case it was changed in the child app
	if (settings.logLevel) {
		loggingLevel = settings.logLevel.toInteger()
	} else {
		loggingLevel = 3
	}
	
	// Log level was set to a higher level than 3, drop level to 3 in x number of minutes
	if (loggingLevel > 3) {
		logger("trace", "Initialize runIn $settings.logDropLevelTime")
		runIn(settings.logDropLevelTime.toInteger() * 60, logsDropLevel)
	}

	logger("warn", "App logging level set to $loggingLevel")
	logger("trace", "Initialize LogDropLevelTime: $settings.logDropLevelTime")

	// Set device settings
	child.setLogLevel(loggingLevel)

    delay = settings.delay.toInteger()
	// Subscribe to the new sensor(s) and device
	subscribe(tv_sensor, "power", powerHandlerTV)
	subscribe(sound_sensor, "power", powerHandlerSound)
	subscribe(child, "refresh", refreshHandler)
    subscribe(child, "commandToSend", commandHandler)
}


//************************************************************
// getChild
//     Gets current childDeviceWrapper from list of childs
//
// Signature(s)
//     getChild()
//
// Parameters
//     None
//
// Returns
//     ChildDeviceWrapper
//
//************************************************************
def getChild() {
	
	// Does this instance have a DeviceID
	if (!state.deviceID){
		
		//No DeviceID available what is going on, has the device been removed?
		logger("error", "getChild cannot access deviceID!")
	} else {
		
		//We have a deviceID, continue and return ChildDeviceWrapper
		logger("trace", "getChild for device " + state.deviceID)
		def child = getChildDevices().find {
			d -> d.deviceNetworkId.startsWith(state.deviceID)
		}
		logger("trace","getChild child is ${child}")
		return child
	}
}


//************************************************************
// powerHandler
//     Handles a sensor power change event
//     Do not call this directly, only used to handle events
//
// Signature(s)
//     powerHandler(evt)
//
// Parameters
//     evt : passed by the event subsciption
//
// Returns
//     None
//
//************************************************************
def powerHandlerTV(evt)
{
	logger("debug", "TV power changed to" + evt.doubleValue)
	updatePower()
}
def powerHandlerSound(evt)
{
	logger("debug", "Sound power changed to" + evt.doubleValue)
	updatePower()
}

//************************************************************
// updatePower
//     Update current power based on selected sensors
//
// Signature(s)
//     updatePower()
//
// Parameters
//     None
//
// Returns
//     None
//
//************************************************************
def updatePower() {
	def child=getChild()
    def tv_power = 0
    def sound_power = 0
    if (tv_sensor != null) {
        child.setPowerTV(tv_sensor.currentValue("power"))
    }
    if (sound_sensor != null) {
        child.setPowerSound(sound_sensor.currentValue("power"))
    }
    return tv_power + sound_power
}

def refreshHandler() {
    updatePower()
}

def commandHandler(evt) {
    sendCommand(evt.value)
}
def sendCommand(commandName)
{
    def child = getChild()
    if (commandName == "null") { return }
    child.logger("trace", "sendCommand:" + commandName)
    def command = null
    def remote = null
    String[] str = commandName.split(' ')
    def r = str[0]
    def command_list = str[1]
    child.logger("trace", "remote to use:" + r)
    child.logger("trace", "command to send:" + command_list)
    String[] commands = command_list.split(',')
    for (c in commands) {
        if (r == "tv") {
            child.logger("trace", "sendding tv command:" + c)
            tv_remote.push(c)
        }
        if (r == "sound") {
            child.logger("trace", "sendding sound command:" + c)
            sound_remote.push(c)
        }
        pauseExecution(settings.delay)
    }
    child.setLastCommand(commandName)
    child.resetCommandToSend()
}

//************************************************************
// logger
//     Wrapper function for all logging with level control via preferences
//
// Signature(s)
//     logger(String level, String msg)
//
// Parameters
//     level : Error level string
//     msg : Message to log
//
// Returns
//     None
//
//************************************************************
def logger(level, msg) {
	switch(level) {
		case "error":
			if (loggingLevel >= 1) log.error msg
			break

		case "warn":
			if (loggingLevel >= 2) log.warn msg
			break

		case "info":
			if (loggingLevel >= 3) log.info msg
			break

		case "debug":
			if (loggingLevel >= 4) log.debug msg
			break

		case "trace":
			if (loggingLevel >= 5) log.trace msg
			break

		default:
			log.debug msg
			break
	}
}


//************************************************************
// logsDropLevel
//     Turn down logLevel to 3 in this app/device and log the change
//
// Signature(s)
//     logsDropLevel()
//
// Parameters
//     None
//
// Returns
//     None
//
//************************************************************
def logsDropLevel() {
	def child=getChild()
	
	app.updateSetting("logLevel",[type:"enum", value:"3"])
	child.setLogLevel(3)
	
	loggingLevel = app.getSetting('logLevel').toInteger()
	logger("warn","App logging level set to $loggingLevel")
}

