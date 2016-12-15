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
 *  ====KNOWN ISSUES====
 *	1. The SmartPlug shuts off too soon. right now it's hard coded and if computer takes longer than 2 minutes to
 *     shutdown it will perform a hard shutdown.
 *  
 */
definition(
    name: "Computer Power",
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
    
    /*
    section("Smart Outlet") {
    	//used for testing
    	input "thepower", "capability.switch", required: true, title: "Smart Outlet"
    }
    */
    
    section("Smart Plug with Power Meter") {
    	// Verifies computer is offline and shuts off thepower
    	input "themeter", "capability.powerMeter", required: true, title: "Power Meter"
    }
    
    section("Computer Settings") {
    	input "computerIP", "text", required: true, title: "Computer IP Address", description: "Enter the IP address of your computer."
        input "computerPort", "number", required: true, title: "Web Server port", description: "Enter the port of your EventGhost web server."
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
	subscribe(theswitch,"switch.on",theswitchOnHandler)
    subscribe(theswitch,"switch.off",theswitchOffHandler)
}

def theswitchOnHandler(evt) {
	log.debug "theswitchOnHandler: $evt"
    //if power is off turn on power
    //turn thepower on
    setPowerOn()
}

def theswitchOffHandler(evt) {
	log.debug "theswitchOffHandler: $evt"
    shutdownComputer()
    runIn(30*4,setPowerOff)
}

def getMeterValue() {
	def meterValue = themeter.currentPower as int
    return meterValue
}

def setPowerOn() {
	themeter.on()
}

def setPowerOff() {
	themeter.off()
}

def shutdownComputer(evt) {
	def egHost = computerIP + ":" + computerPort
    log.debug "$egHost"
	def egRawCommand = "ST.PCPower.Shutdown"
	def egRestCommand = java.net.URLEncoder.encode(egRawCommand)
	log.debug "egRestCommand:  $egRestCommand"
	sendHubCommand(new physicalgraph.device.HubAction("""GET /?$egRestCommand HTTP/1.1\r\nHOST: $egHost\r\n\r\n""", physicalgraph.device.Protocol.LAN))
}
// TODO: implement event handlers