# Project Description


An application that runs on a Raspberry Pi W. It is designed to connect via HDMI to a screen and present an intellegent slideshow.

We will also develop simple buttons which will allow passers by to pause the slide show. (Maybe back and forward as well.)

## Prokect Aims

We have chosen the design and technology used in this project to help us achieve the following aims:
 - Gain a more in depth understanding of Clojure
 - Learn about and play with configuration as code
 - Learn about and play with Raspberry Pi's
 - Learn about modern development practices
 - Learn about github and how forking, pull requests etc. work
 - We should make it as easy as possible for more developers to contribute to the project

## Modules

The base system should be extensible with modules. This will help us make it easy for more developers to contribute to the project as they should be able to write independant modules to extend the initial functionality.
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

## Current Tasks

Currently we are in the process of conducting proof of concept tests.
Completed proof of concept tests are to be able to present a picture file to the screen using clojure.
