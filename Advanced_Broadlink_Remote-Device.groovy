/*
 *  Advanced Broadlink Remote Driver
 *  Project URL: https://github.com/mboisson/Advanced_Broadlink_Remote
 *  Copyright 2024 Maxime Boissonneault
 *
 *  This driver is not meant to be used by itself, please go to the project page for more information.
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

metadata {
	definition (name: "Advanced Broadlink Remote Device", 
		namespace: "mboisson", 
		author: "Maxime Boissonneault",
		importUrl: "https://raw.githubusercontent.com/mboisson/Advanced_Broadlink_Remote/main/Advanced_Broadlink_Remote-Device.groovy") {
		
		capability "Switch"
		capability "PowerMeter"
		capability "Configuration"
        
        command "up"
        command "down"
        command "custom1"
        command "custom2"
        command "push", ["name"]
        
        attribute "last_command", "string"
        attribute "commandToSend", "string"
	}
    
    preferences {
				input "onCommand", "string", title: "Command(s) to use to turn device on", required: false
				input "offCommand", "string", title: "Command(s) to use to turn device off", required: false
				input "powerThresholdOn", "decimal", title: "Power value above which the device is considered on (in W)", required: false, defaultValue: 10
				input "up", "string", title: "Command(s) to use for up", required: false
				input "down", "string", title: "Command(s) to use for down", required: false
				input "custom1", "string", title: "Custom command(s) 1", required: false
				input "custom2", "string", title: "Custom command(s) 2", required: false
    }
}

//************************************************************
//************************************************************
def configure() {
    sendEvent(name: "switch", value:"off")
}

def push(name) {
    sendEvent(name: "commandToSend", value:name)
}
def up() {
    sendEvent(name: "commandToSend", value:settings.up)
}
def down() {
    sendEvent(name: "commandToSend", value:settings.down)
}
def custom1() {
    sendEvent(name: "commandToSend", value:settings.custom1)
}
def custom2() {
    sendEvent(name: "commandToSend", value:settings.custom2)
}
def resetCommandToSend() {
    sendEvent(name: "commandToSend", value:"null")
}


//************************************************************
//************************************************************
def installed() {
    // nothing to do
    logger("trace", "installed() - Nothing to do")
}


//************************************************************
//************************************************************
def updated() {
    logger("trace", "updated() - Nothing to do")
}



//************************************************************
//************************************************************
def parse(String description) {
	// Nothing to parse here since this is a virtual device
}


//************************************************************
// off
//     Set switch to "off"
// Signature(s)
//     off()
// Parameters
//     None
// Returns
//     None
//************************************************************
def off() {
	if (device.currentValue("switch") != "off") {
		logger("trace", "off() - sendEvent")
		sendEvent(name: "switch", value: "off")
		sendEvent(name: "commandToSend", value:settings.offCommand)
	} else {
		logger("trace", "off() - already set")
	}
}
//************************************************************
// on
//     Set switch to "on"
// Signature(s)
//     on()
// Parameters
//     None
// Returns
//     None
//************************************************************
def on() {
	if (device.currentValue("switch") != "on") {
		logger("trace", "on() - sendEvent")
		sendEvent(name: "switch", value: "on")
        sendEvent(name: "commandToSend", value:settings.onCommand)
	} else {
		logger("trace", "on() - already set")
	}
}
def setLastCommand(val) {
	sendEvent(name: "last_command", value: val)
}

//************************************************************
// Poll
//     Do nothing, maybe in the future?
// Signature(s)
//     poll()
// Parameters
//     None
// Returns
//     None
//************************************************************
def poll() {
	logger("trace", "poll() - Nothing to do")
	null
}

//************************************************************
// Refresh
//     Do nothing, maybe in the future?
// Signature(s)
//     refresh()
// Parameters
//     None
// Returns
//     None
//************************************************************
def refresh() {
  logger("trace", "refresh() - Nothing to do")
}


//************************************************************
// setPower
//     Set power directly
//     Called from the manager app
// Signature(s)
//     setPower(value)
// Parameters
//     value : 
// Returns
//     None
//************************************************************
def setPower(value) {
	logger("trace", "setPower($value) - sendEvent")
	sendEvent(name:"power", value: value, unit: units)
    if (settings.powerThresholdOn != null) {
        if (value > settings.powerThresholdOn) {
            if (device.currentValue("switch") != "on") {
                sendEvent(name:"switch", value:"on")
            }
        }
        else {
            if (device.currentValue("switch") != "off") {
                sendEvent(name:"switch", value:"off")
            }
        }
    }
}
//************************************************************
// logger
//     Wrapper function for all logging with level control via preferences
// Signature(s)
//     logger(String level, String msg)
// Parameters
//     level : Error level string
//     msg : Message to log
// Returns
//     None
//************************************************************
def logger(level, msg) {

	switch(level) {
		case "error":
			if (state.loggingLevel >= 1) log.error msg
			break

		case "warn":
			if (state.loggingLevel >= 2) log.warn msg
			break

		case "info":
			if (state.loggingLevel >= 3) log.info msg
			break

		case "debug":
			if (state.loggingLevel >= 4) log.debug msg
			break

		case "trace":
			if (state.loggingLevel >= 5) log.trace msg
			break

		default:
			log.debug msg
			break
	}
}


//************************************************************
// setLogLevel
//     Set log level via the child app
// Signature(s)
//     setLogLevel(level)
// Parameters
//     level :
// Returns
//     None
//************************************************************
def setLogLevel(level) {
	state.loggingLevel = level.toInteger()
	logger("warn","Device logging level set to $state.loggingLevel")
}


