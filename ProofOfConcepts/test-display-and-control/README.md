# test-display-and-control

Sample project started by RJM to demo:
 - Seesaw clock
 - playing about with options for module system
 - Slack integration


## Installation

Clone this repo

You will need to create a local config.edn which is not ever uploaded to git. This file will include the slack credentials used by the program to talk to slack.

### Create a new slack chatbot

 - Log into the slack channel
 - Under 'Apps' press +
 - Press Manage apps
 - Select 'Custom Integrations'
 - Select 'Bots'
 - Note the bots we already have and decide on a free username. 
 - Select 'Add Configuration'
 - Add user name simulare to 'testbot99-poc-rjmwork'
 - Press Add bot integration
 - Fill in other details and note down the API key
 - If you want give the bot an icon, description etc.
 - Save integration
 - copy config.edn.example to config.edn
 - Edit the file adding the API key

 As well as talking to the bot directly you can add it to the testbotchannel.

## Usage

````
lein run
````

## Options


...

