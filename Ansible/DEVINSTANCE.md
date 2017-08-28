# Set up a development instances

## 1. Get up and running with a hostname and connected to the internet

The following are the steps that can be followed to setup a Raspberry Pi.

Get a Raspbery Pi Zero W (or other Raspberry Pi Model)
write 2017-07-05-raspbian-jessie.img image to a SD card. (I used 16Gb)
In the Boot partition create a file called "ssh" with zero contents

For the network connection you can either
 - Use wifi by configuring /etc/wpa_supplicant (My notes on this https://code2.metcarob.com/node/235)
 - USe USBOTG (My notes on this https://code2.metcarob.com/node/307)

Place card in Raspberry Pi Zero W and let it boot. First time will take a while as it will resize the partition.

Once the Raspberry Pi has booted SSH into it using the user pi and the password raspberry.

Follow the following steps:
- create ~/.ssh directory and add my public ssh key to ~/.ssh/authorized_keys (Then check by logging off and on)
- use raspi-config to set localization options timezone to europe/london
- set hostname to desired hostname
- Turn on vnc (it's under interfacing options)
- reboot so all settings take effect

We are leaving the password login method open at this point - we will alter it once we have configured VNC server


# 3. Create a fork of the project

In order to start playing with the code you should create you own 'fork' of this project on github. If you don't have a github account create one, then browse to the proejct and 'XXXXX'

TODO

# 2. Run ansible dev_instance playbook

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


# 3. Use remote desktop to connect to the Respberry Pi

To connect I had to use realVNC (We should find an alternative)
https://www.realvnc.com/en/connect/download/viewer

on the instance run raspi-config
then goto 
7 Advanced options
A5 Resolution
DMT Mode 9  800x600 60Hz 4:3 (My choice - this is just for testing)

Use RealVNC viewer to connect to the raspberry pi using user pi and password raspberry.

TODO Security configuration for RealVNC (or switch to alternative vnc server)




# X. Disable normal password access

Once SSH login and VNC login is confirmed shut off normal password access updating /etc/ssh/sshd_config:
````
PasswordAuthentication no
````

Also it is a good idea to run passwd and change the pi user password from raspberry to a random value that dosen't need to be kept.



