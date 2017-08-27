# Instructions for setting up instances

An instance of RaspPiClojureScreens is a Raspberry PI device with the software running.

## Setup of Configuration machine

You need to use a main configuration machine (normally a laptop) which you use to create instances.
Install Ansible on this machine


## Set up a development instances

### 1. Get up and running with a hostname and connected to the internet

The following are the steps that can be followed to setup a Raspberry Pi.

Get a Raspbery Pi Zero W (or other Raspberry Pi Model)
write 2017-07-05-raspbian-jessie.img image to a SD card. (I used 16Gb)
In the Boot partition create a file called "ssh" with zero contents

Setup wifi by entering my SSID and password to /etc/wpa_supplicant in the sd card.

Place card in Raspberry Pi Zero W and let it boot. First time will take a while as it will resize the partition.

Once the Raspberry Pi has booted SSH into it using the user pi and the password raspberry.

Follow the following steps:
- create ~/.ssh directory and add my public ssh key to ~/.ssh/authorized_keys (Then check by logging off and on)
- Once I have confirmed ssh key based login is working I disable passwordl log in by updating /etc/ssh/sshd_config:
````
PasswordAuthentication no
````
- run passwd and change the pi user password from raspberry to a random value I will never use.
- use raspi-config to set localization options timezone to europe/london
- set hostname to desired hostname
- reboot so all settings take effect

### 2. Run ansible dev_instance playbook

TODO

