/**
 *  Computer Power Control
 *
 *  Copyright 2016 Matt Sutton
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
    name: "Computer Power Control",
    namespace: "freethewhat",
    author: "Matt Sutton",
    description: "Powers on a computer using a smart outlet. Shuts the computer down using EventGhost webservice and powers off the smart outlet after 2 minutes.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Simulated Switch") {
		// used to initiate shutdown of PC and power off when themeter is below threshold.
		// If integrated with Amazon Echo or Google Home. Name it after the device (eg. Computer, 
		// Desktop Computer, Entertainment Center, etc.)
		input "theswitch", "capability.switch", required: true, title: "Switch"
	}
    
    	section("Smart Plug with Power Meter") {
		// Verifies computer is offline and shuts off thepower
		input "themeter", "capability.powerMeter", required: true, title: "Power Meter"
   	 }
    
  	section("Computer Settings") {
		input "computerIP", "text", required: true, title: "Computer IP Address", description: "Enter the IP address of your computer."
		input "computerPort", "number", required: true, title: "Web Server port", description: "Enter the port of your EventGhost web server."
		input "meterthreshold", "number", required: true, title: "Shutdown Threshold", description: "If the meter drops below this threshold the SmartPort will shutdown."
    	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	state.lastMeter = 0
	state.currentMeter = 0
	subscribe(theswitch,"switch.on",theswitchOnHandler)
	subscribe(theswitch,"switch.off",theswitchOffHandler)
}

def theswitchOnHandler(evt) {
	log.debug "theswitchOnHandler: $evt"
	schedule("* * * * * ?", getMeterValue)
	setPowerOn()
}

def theswitchOffHandler(evt) {
	log.debug "theswitchOffHandler: $evt"
	shutdownComputer()
	log.debug "theswitchOffHandler: Shutdown Computer"
	schedule("* * * * * ?", setPowerOff)
}

def getMeterValue() {
	log.debug "Cronjob getMeterValue: Run"
	themeter.refresh()
	state.lastMeter = state.currentMeter
	log.debug "Cronjob getMeterValue: last Meter = $state.lastMeter"
	state.currentMeter = themeter.currentPower as int
	log.debug "Cronjob getMeterValue: current Meter = $state.currentMeter"    
}

def setPowerOn() {
	themeter.on()
}
 
def setPowerOff() {
	log.debug "Cronjob setPowerOff: Ran"
	def shutdownReady = false
    
	if(state.currentMeter <= meterthreshold && state.lastMeter <= meterthreshold){
		log.debug "Cronjob setPowerOff: shutdownReady is true"
		shutdownReady = true
    	}
	
	if(shutdownReady){
		themeter.off()
		unschedule(getMeterValue)
		unschedule(setPowerOff)
		state.lastMeter = 0
		state.currentMeter = 0
	}
}

def shutdownComputer(evt) {
	def egHost = computerIP + ":" + computerPort
	def egRawCommand = "ST.PCPower.Shutdown"
	def egRestCommand = java.net.URLEncoder.encode(egRawCommand)
	sendHubCommand(new physicalgraph.device.HubAction("""GET /?$egRestCommand HTTP/1.1\r\nHOST: $egHost\r\n\r\n""", physicalgraph.device.Protocol.LAN))
}
// TODO: implement event handlers
