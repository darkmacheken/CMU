# CMU - 2018/2019
Mobile and Ubiquitous Computing Project - P2Photo

## Requirements
You must have installed the following tools:
- Java Development Kit 8+ (JDK 8+)
- Android SDK 26+
- Nodejs 10+
- TypeScript 3.3+
- ExpressJS 4.16+

Also check if JAVA_HOME is set properly

## Getting Started with Nodejs
When you install nodejs, it comes with the Nodejs Package Manager (npm for short).

### Install TypeScript
To install typescript globally just run the following command:

```
    npm install typescript -g
```

### Install ExpressJS
To install ExpressJS go to the folder **/server/** and run the command:

```
    npm install express -s
```

It will generate a folder named **node_modules** with all the necessary modules that Express uses to work.

Express and Typescript packages are independent. The consequence of this is that Typescript does not “know” types of Express classes. 
There is a specific npm package for the Typescript to recognize the Express types.
```
    npm install @types/express -s
```

## How to Compile and Run
### Mobile Application
If you have Android Studio, just build inside the IDE. If not, you can build the project usiong the terminal, check this [guide](https://developer.android.com/studio/build/building-cmdline) how to use gradle to build an android project.

To run, you should use the android emulator or deploy in an android device.

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
