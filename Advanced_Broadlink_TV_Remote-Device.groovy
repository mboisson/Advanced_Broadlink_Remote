/*
 *  Advanced Broadlink TV Remote Driver
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
	definition (name: "Advanced Broadlink TV Remote Device", 
		namespace: "mboisson", 
		author: "Maxime Boissonneault",
		importUrl: "https://raw.githubusercontent.com/mboisson/Advanced_Broadlink_Remote/main/Advanced_Broadlink_TV_Remote-Device.groovy") {
		
		capability "Switch"
		capability "PowerMeter"
        capability "TV"
		capability "Configuration"
        
        command "input1"
        command "input2"
        command "mute"
        command "unmute"
        command "push", ["name"]
        command "nextTrack"
        command "previousTrack"
        
        attribute "last_command", "string"
        attribute "commandToSend", "string"
        attribute "switchTV", "string"
        attribute "switchSound", "string"
        attribute "powerTV", "decimal"
        attribute "powerSound", "decimal"
        attribute "mute", "string"
	}
    
    preferences {
				input "onCommandTV", "string", title: "Command to use to turn TV on", required: false
				input "offCommandTV", "string", title: "Command to use to turn TV off", required: false
				input "powerThresholdOnTV", "decimal", title: "Power value above which the TV is considered on (in W)", required: false, defaultValue: 10
				input "channelUp", "string", title: "Command to use for channel up", required: false
				input "channelDown", "string", title: "Command to use for channel down", required: false
				input "input1", "string", title: "Command to set to input 1", required: false
				input "input2", "string", title: "Command to set to input 2", required: false
				input "onCommandSound", "string", title: "Command to use to turn sound system on", required: false
				input "offCommandSound", "string", title: "Command to use to turn sound system off", required: false
				input "volumeUp", "string", title: "Command to use for volume up", required: false
				input "volumeDown", "string", title: "Command to use for volume down", required: false
				input "mute", "string", title: "Command to use for mute", required: false
				input "unmute", "string", title: "Command to use for unmute", required: false
				input "powerThresholdOnSound", "decimal", title: "Power value above which the sound system is considered on (in W)", required: false, defaultValue: 10
    }
}

//************************************************************
//************************************************************
def configure() {
    sendEvent(name: "switch", value:"off")
    sendEvent(name: "mute", value: "unmuted")
}

def push(name) {
    sendEvent(name: "commandToSend", value:name)
}
def channelUp() {
    sendEvent(name: "commandToSend", value:"tv " + settings.channelUp)
}
def nextTrack() {
    sendEvent(name: "commandToSend", value:"tv " + settings.channelUp)
}
def channelDown() {
    sendEvent(name: "commandToSend", value:"tv " + settings.channelDown)
}
def previousTrack() {
    sendEvent(name: "commandToSend", value:"tv " + settings.channelDown)
}
def volumeUp() {
    sendEvent(name: "commandToSend", value:"sound " + settings.volumeUp)
    sendEvent(name: "mute", value:"unmuted")
}
def volumeDown() {
    sendEvent(name: "commandToSend", value:"sound " + settings.volumeDown)
    sendEvent(name: "mute", value:"unmuted")
}
def mute() {
    if (device.currentValue("mute") == "unmuted") {
        sendEvent(name: "mute", value: "muted")
        sendEvent(name: "commandToSend", value:"sound " + settings.mute)
    }
}
def unmute() {
    if (device.currentValue("mute") == "muted") {
        sendEvent(name: "mute", value: "unmuted")
        sendEvent(name: "commandToSend", value:"sound " + settings.unmute)
    }
}
def input1() {
    sendEvent(name: "commandToSend", value:"tv " + settings.input1)
}
def input2() {
    sendEvent(name: "commandToSend", value:"tv " + settings.input2)
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
	} else {
		logger("trace", "off() - already set")
	}
    offTV()
    runIn(5, offSound)
}
//************************************************************
// offTV
//     Set TV switch to "off"
// Signature(s)
//     offTV()
// Parameters
//     None
// Returns
//     None
//************************************************************
def offTV() {
	if (device.currentValue("switchTV") != "off") {
		logger("trace", "offTV() - sendEvent")
		sendEvent(name: "switchTV", value: "off")
        sendEvent(name: "commandToSend", value:"tv " + settings.offCommandTV)
	} else {
		logger("trace", "offTV() - already set")
	}
}
//************************************************************
// offSound
//     Set Sound switch to "off"
// Signature(s)
//     offSound()
// Parameters
//     None
// Returns
//     None
//************************************************************
def offSound() {
	if (device.currentValue("switchSound") != "off") {
		logger("trace", "offSound() - sendEvent")
		sendEvent(name: "switchSound", value: "off")
        sendEvent(name: "commandToSend", value:"sound " + settings.offCommandSound)
	} else {
		logger("trace", "offSound() - already set")
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
	} else {
		logger("trace", "on() - already set")
	}
    onTV()
    runIn(5, onSound)
}
//************************************************************
// onTV
//     Set switchTV to "on"
// Signature(s)
//     onTV()
// Parameters
//     None
// Returns
//     None
//************************************************************
def onTV() {
	if (device.currentValue("switchTV") != "on") {
		logger("trace", "onTV() - sendEvent")
		sendEvent(name: "switchTV", value: "on")
        sendEvent(name: "commandToSend", value:"tv " + settings.onCommandTV)
	} else {
		logger("trace", "onTV() - already set")
	}
}
//************************************************************
// onSound
//     Set switchSound to "on"
// Signature(s)
//     onSound()
// Parameters
//     None
// Returns
//     None
//************************************************************
def onSound() {
	if (device.currentValue("switchSound") != "on") {
		logger("trace", "on() - sendEvent")
		sendEvent(name: "switchSound", value: "on")
        sendEvent(name: "commandToSend", value:"sound " + settings.onCommandSound)
	} else {
		logger("trace", "onSound() - already set")
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
}
//************************************************************
// setPowerTV
//     Set powerTV directly
//     Called from the manager app
// Signature(s)
//     setPowerTV(value)
// Parameters
//     value : 
// Returns
//     None
//************************************************************
def setPowerTV(value) {
	logger("trace", "setPowerTV($value) - sendEvent")
	sendEvent(name:"powerTV", value: value, unit: units)
    if (settings.powerThresholdOnTV != null) {
        if (value > settings.powerThresholdOnTV) {
            if (device.currentValue("switchTV") != "on") {
                sendEvent(name:"switchTV", value:"on")
                sendEvent(name:"switch", value:"on")
            }
        }
        else {
            sendEvent(name:"switchTV", value:"off")
            if (device.currentValue("switchSound") == "off") {
                sendEvent(name:"switch", value:"off")
            }

        }
    }
}
//************************************************************
// setPowerSound
//     Set powerSound directly
//     Called from the manager app
// Signature(s)
//     setPowerSound(value)
// Parameters
//     value : 
// Returns
//     None
//************************************************************
def setPowerSound(value) {
	logger("trace", "setPowerSound($value) - sendEvent")
	sendEvent(name:"powerSound", value: value, unit: units)
    if (settings.powerThresholdOnSound != null) {
        if (value > settings.powerThresholdOnSound) {
            if (device.currentValue("switchSound") != "on") {
                sendEvent(name:"switchSound", value:"on")
                sendEvent(name:"switch", value:"on")
            }
        }
        else {
            sendEvent(name:"switchSound", value:"off")
            if (device.currentValue("switchTV") == "off") {
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


