# RaspPiClojureScreens
An application that runs on a Raspberry Pi W. It is designed to connect via HDMI to a screen and present an intellegent slideshow.

We will also develop simple buttons which will allow passers by to pause the slide show. (Maybe back and forward as well.)

## Modules

The slideshow can be made of modules. We could have modules for displaying Images, querying services and rendering them, etc.

## Control

The system will register itself as a slack bot and will connect to slack and respond to commands. Commands can be given to:
 - Quit the application
 - Load a new module (and start displaying it's content)
 - List loaded modules
 - Disable a module
 - Terminate a module

We have setup a test slack channel to play with: https://rjmslackexp.slack.com

## Clojure

We want to use this project to learn about clojure which is the basis for chosing to develop in this.

