# Instructions for setting up instances

These instructions describe how to setup the Raspberry PI's to run an 'instance' of RasPiClojureScreens. You must first set up you configuration machine which is the computer you use to run the configuration as code process. Once you have done this you may set up a 'development instance' which will include a git clone of the project allowing you to play about with it and contribute.
When we are ready we will expand this to also provide a process to setup a production instance.

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
- Once I have confirmed ssh key based login is working I disable password login by updating /etc/ssh/sshd_config:
````
PasswordAuthentication no
````
- run passwd and change the pi user password from raspberry to a random value I will never use.
- use raspi-config to set localization options timezone to europe/london
- set hostname to desired hostname
- reboot so all settings take effect

### 3. Create a fork of the project

In order to start playing with the code you should create you own 'fork' of this project on github. If you don't have a github account create one, then browse to the proejct and 'XXXXX'

TODO

### 2. Run ansible dev_instance playbook

Generate a public and private ssh key pair. You will need to upload the public key to your git account so that it is able to take a clone of the repo. I use a speciall generated key pair for this so that I can revoke access from the RaspberryPi Instances seperatly to my main one.

Run the ansible command:
````
ansible-playbook -i "usbpi," dev_instance.yml --extra-vars "git_public_ssh_key_path=/home/robert/.ssh/id_rsa_infrastructure.pub git_private_ssh_key_path=/home/robert/.ssh/id_rsa_infrastructure project_repo=git@github.com:rmetcalf9/RaspPiClojureScreens.git"
````

Replace usbpi with the hostname of the Raspberry Pi you are configuring the instance on.

You will have to replace the extra-vars as follows:
git_public_ssh_key_path - path to the public key you uploaded to git (on your configuration machine)
git_private_ssh_key_path - path to the private key that matches the public key uploaded to git
project_repo - change to your own fork of the project repo


### 3. Use remote desktop to connect to the Respberry Pi

VNC



