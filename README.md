# CMU - 2018/2019
Mobile and Ubiquitous Computing Project - P2Photo

## Requirements
The target platform for P2Photo is Android version >= 4.0.3 (API level 15+). To build and run the project, you must have installed the following tools:
- Java Development Kit 8+ (JDK 8+)
- Android SDK 15+
- Google Play services 49+ ([download and configure](https://developer.android.com/google/play-services/setup.html))  
- Nodejs 10.0.0+
- Node Package Manager (npm)

Necessary node packages:
- TypeScript 3.3+
- ExpressJS 4.16+
- Passport 0.4.0+
- Passport-jwt 4.0.0+
- Passport-local 1.0.0+
- Google-Auth-Library 3.1.2+
- GoogleApis 39.2.0+
- UUID 3.3.0+

## Getting Started with Nodejs
When you install nodejs, it comes with the Nodejs Package Manager (npm for short).

### Install TypeScript
To install typescript globally just run the following command:

```
    npm install typescript -g
```

### Install Dependecies
To install all dependecies shown in the requirements, go to the folder **/server/** and run the command:

```
    npm install -s
```

It will generate a folder named **node_modules** with all the necessary modules that the server uses to work.

## How to Compile and Run
### Server
The code of the server runs in nodejs and node can't run typescript, so it's needed to transpile the _ts_ code to _js_ code. To do that, you can must be in the folder **/server/** and run the command.

```
    tsc
```

After transpilation, the javascript files will be generated under **/server/dist/**.
In the folder **/server/** run the command:
```
    node dist/main.js
```

There is a configurated script that compiles and runs the server (previous 2 steps in one), in folder **/server/** run the command:
```
    npm start
```

The server is, by default, running in port **8443**.

Note: It uses HTTPS, so you **MUST** specify the protocol when requesting the server (e.g. **https**://localhost:8443/users), otherwise it won't work.

### Mobile Application
#### Change IP
The mobile application, for cloud storage, has to connect to the server. As the server is usually running in your computer (with a dynamic IP address), you need to change the URL in the file strings under **/mobile/app/src/main/res/values/strings.xml** the value **server_url** to match with your IP:

```xml
    <string name="server_url">https://192.168.1.67:8443</string>
```

After that, you can build the application. If you have Android Studio, just build it inside the IDE. If not, you can build the project using the terminal, check this [guide](https://developer.android.com/studio/build/building-cmdline) how to use gradle to build an android project.

To run, you should use the android emulator or deploy the apk in an android device.
