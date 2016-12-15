/**
 *  SmartPlug Meter Reader
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
 *
 *
 *   .---------------- minute (0 - 59)
 *   |  .------------- hour (0 - 23)
 *   |  |  .---------- day of month (1 - 31)
 *   |  |  |  .------- month (1 - 12) OR jan,feb,mar,apr ...
 *   |  |  |  |  .---- day of week (0 - 6) (Sunday=0 or 7) OR sun,mon,tue,wed,thu,fri,sat
 *   |  |  |  |  |
 *   *  *  *  *  * user-name  command to be executed
 *
 **/
 
def n = 0

definition(
    name: "SmartPlug Meter Reader",
    namespace: "freethewhat",
    author: "Matt Sutton",
    description: "Monitor smart plug power usage.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {

	section("SmartPlug Outlet") {
		input "themeter", "capability.powerMeter", required: true
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
	subscribe(themeter,"switch.on",turnOnMonitor)
    subscribe(themeter,"switch.off",turnOffMonitor)
}

def turnOnMonitor(evt){
	log.debug "themeter is powered on $evt"
	schedule("* * * * * ?", meterRefresh)
}

def turnOffMonitor(evt){
	log.debug "themeter is powered off. $evt"
	unschedule(meterRefresh)
}

def meterRefresh() {
	log.debug "Cron job run"
	themeter.refresh()
}
