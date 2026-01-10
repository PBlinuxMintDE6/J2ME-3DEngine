# J2ME-3D Engine
An 3D game engine designed for the Java 2 Mobile Edition platform

## Why?
I wanted to learn Java (more specifically J2ME) and though that this project would be perfect to help.
I've also always wanted to write a game engine.

## How?
The renderer takes in a set of 3D coordinates in world space and projects them onto camera space
It then further projects the camera space coordinates onto screen coordinates
A filled triangle is then drawn there.
In a few 'simple' steps, we have taken a 3D scene and rendered it. The rest is just extra.

## How to run?
Currently since no builds are available, you'll need to use NetBeans 7.4, Java Development Kit 8_92 and Java ME SDK 3.4 to build.
Testing of this application is done using the ME emulator included with the Java ME SDK specfically on the profile "JavaMEPhone1".
Testing is also done using J2ME_Loader on Android 4.0.4

### Building instructions
- With the requirements installed (Java Development Kit 8_92, Java ME SDK 3.4) and configured
- Using NetBeans 7.4 clone this git repository
- Once NetBeans 7.4 finishes importing the project select Run -> Clean and Build Project
- A .jad and .jar file should be available at <PROJECT_DIRECTORY>/dist
