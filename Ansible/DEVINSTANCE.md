# Set up a development instances

## 1. Get up and running with a hostname and connected to the internet

The following are the steps that can be followed to setup a Raspberry Pi.

Get a Raspbery Pi Zero W (or other Raspberry Pi Model)
write 2017-07-05-raspbian-jessie.img image to a SD card. (I used 16Gb)
In the Boot partition create a file called "ssh" with zero contents

For the network connection you can either
 - Use wifi by configuring /etc/wpa_supplicant (My notes on this https://code2.metcarob.com/node/235)
 - Use USBOTG (My notes on this https://code2.metcarob.com/node/307)

Place card in Raspberry Pi Zero W and let it boot. First time will take a while as it will resize the partition.

Once the Raspberry Pi has booted SSH into it using the user pi and the password raspberry:

Follow the following steps:
- create ~/.ssh directory and add my public ssh key to ~/.ssh/authorized_keys (Then check by logging off and on)
- use raspi-config to set localization options timezone to europe/london
- use raspi-config to set hostname to desired hostname
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

 | Name                     | Explination  | 
 |:-------------------------|:------------|
 | git_public_ssh_key_path  | path to the public key you uploaded to git (on your configuration machine)
 | git_private_ssh_key_path | path to the private key that matches the public key uploaded to git
 | project_repo             | change to your own fork of the project repo


# 3. Use remote desktop to connect to the Respberry Pi

Raspbian comes pre-installed with a vnc server called RealVNC. This has a very restrictive license which requires registing for an online account to use. For this reason it is beter for us to use tightVNC which is a free opensource one availaible online. The ansible script will do the nessecary removal and instalisation however there are a few extra steps required to setup the password and screen resolution. (Described below)

Source I refered to when creating the process for tightVNC (https://www.childs.be/blog/post/remove-real-vnc-from-raspberrypi-pixel-and-install-tightvnc)


ssh into the raspberry pi
type
````
tightvncserver
````
configure it with a use and password.

enter an 8 digit password
do not enter a view only password

I got the following response from the config program:
Creating default startup script /home/pi/.vnc/xstartup
Starting applications specified in /home/pi/.vnc/xstartup
Log file is /home/pi/.vnc/raspiclojuredev001:1.log

You must start a session with the desired gemoetry and colour depth. Find the settings you want and run the following on the Raspberry PI.
````
vncserver -kill :1
vncserver :1 -geometry 1024x612 -depth 16
````

you can connect to HOSTNAME:1 with a vnc client

The first time I ran it I set the background to a solid colour.



# X. Disable normal password access

Once SSH login and VNC login is confirmed shut off normal password access updating /etc/ssh/sshd_config:
````
PasswordAuthentication no
````

Also it is a good idea to run passwd and change the pi user password from raspberry to a random value that dosen't need to be kept.



