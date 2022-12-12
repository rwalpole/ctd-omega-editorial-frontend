# Pan-Archival Editorial Frontend Catalogue

[![Build Status](https://github.com/nationalarchives/ctd-omega-editorial-frontend/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/nationalarchives/ctd-omega-editorial-frontend/actions/workflows/ci.yml)
[![Scala 2.13](https://img.shields.io/badge/scala-2.13-red.svg)](http://scala-lang.org)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

This project is an editorial frontend for managing The National Archives Pan-Archival Catalogue (PAC).

The project is developed in Scala Play framework with [GOV.UK]( https://design-system.service.gov.uk/) Design System toolkit using Twirl implementation of the design system provided by [HMRC](https://github.com/hmrc/play-frontend-hmrc) for building the frontend web application.

## Building from Source Code

### Pre-requisites for building and running the project:
* [Git](https://git-scm.com)
* [sbt](https://www.scala-sbt.org/) >= 1.6.2
* [Scala](https://www.scala-lang.org/) >= 2.13.8
* [Java JDK](https://adoptopenjdk.net/) >= 1.8

## Steps:

1. Clone the Git repository:
```
git clone https://github.com/nationalarchives/ctd-omega-editorial-frontend.git
```
2. Switch to the newly created directory:
```
cd ./ctd-omega-editorial-frontend
```
3. To build the project run the following command:
```
sbt clean compile
```
4. Now run the application
```
sbt run
```
5. Go to a web browser and type the following url to launch the application
```
http://localhost:9000/login
```
6. Enter a valid username and password to access the application

## Publishing a Release to Maven Central

1. Run `sbt clean release`
2. Answer the questions
3. Login to https://oss.sonatype.org/ then Close, and Release the Staging Repository
   25 

## Creating a deployment and running it as a Linux service

1. To create a deployable zip package of the application run:
```
sbt dist
```
2. Copy the zip file create in #1 to the deployment directory, e.g.
```
sudo unzip ./target/universal/ctd-omega-editorial-frontend-0.1.0-SNAPSHOT.zip -d /opt
```
3. Create a ctd-web user which is specifically used to run the service:
```
sudo adduser --system --shell /sbin/nologin ctd-web
```
4. Change the owner of the deployment directory to be the newly created ctd-web user:
```
sudo chown -R ctd-web:ctd-web /opt/ctd-omega-editorial-frontend-0.1.0-SNAPSHOT
```
5. It is recommended to create a symbolc link to the current version as follows:
```
sudo ln -s /opt/ctd-omega-editorial-frontend-0.1.0-SNAPSHOT /opt/ctd-omega-editorial-frontend
```
6. Create a systemd config file as follows, replacing the text `some_application_secret` with a specially generated secret, as described at https://www.playframework.com/documentation/2.8.x/ApplicationSecret#Generating-an-application-secret
```
[Unit]
Description=CTD Omega Editorial Frontend
Documentation=https://github.com/nationalarchives/ctd-omega-editorial-frontend
After=syslog.target

[Service]
Type=simple
User=ctd-web
Group=ctd-web
Environment="APPLICATION_SECRET=some_application_secret"
ExecStart=/opt/ctd-omega-editorial-frontend/bin/ctd-omega-editorial-frontend
Restart=always

[Install]
WantedBy=multi-user.target
```
7. Save the config file as ctd-omega-editorial-frontend.service in the `/etc/systemd/system` directory and ensure that the file is executable by the ctd-user, e.g.
```
sudo chmod 755 /etc/systemd/system/ctd-omega-editorial-frontend.service
```
8. Reload the systemd manager configuration:
```
sudo systemctl daemon-reload
```
9. Enable the ctd-omega-editorial-frontend service:
```
sudo systemctl enable ctd-omega-editorial-frontend
```
10. Start the ctd-omega-editorial-frontend service:
```
sudo systemctl start ctd-omega-editorial-frontend
```
11. Check the application is running by going to http://localhost:9000. You can also check the status using:
```
sudo systemctl status ctd-omega-editorial-frontend
```
12. To view the service log you can use:
```
sudo journalctl -e -u ctd-omega-editorial-frontend
```
13. To check that the application secret and other environment variables are correctly set you can use:
```
sudo strings /proc/<PID>/environ
```
14. To stop the service you can use:
```
sudo systemctl stop ctd-omega-editorial-frontend
```