
This is an extension of the cross platform 3D graphics meta engine for Java in project
https://github.com/thomass171/tcp-22 with components of flightgear. Flightgear (https://www.flightgear.org) is a free
open source flight simulator written in C++ and using OSG under GPL.

Main objective is to have Flightgear scenery and aircraft models 
available in traveling scenes running in a browser (including VR).

Also this is still WiP!

# Building And Deploying

This shows the build and installation to a web browser running locally serving from
directory $HOME/Sites. Set shell variable HOSTDIR, eg.:

```
export HOSTDIR=$HOME/Sites/tcp-22
```
and create that base directory. Build and install tcp-22 to your local maven repos. The build
artifacts are needed here. 

Maven is needed for building. Run

```
mvn clean install
```

for building.

TODO what about ADDITIONALBUNDLES?

# Running
## Browser
Enter the URL
```
http://localhost/<youruserdir>/???
```
in your browser.

# Modules 
## flightgear
Migration of some components of FlightGear to Java:

  + simgear property tree including SGExpressions
  * Scenery loader
  * Model loader
  * Aircraft/Vehicle loader 'FgVehicleLoader'

Flightgear should be a kind of plugin to module 'traffic'.

Design of migration is:
  * Replace OSG by ...
  * Move global property trees to ...
  * TODO: BTG model loading is part of tools, so only available for converting aso.

TODO: extract a core module without engine dependency to have tools-fg without engine dependency.
## tools-fg

## traffic-fg
Extension of base module 'traffic' with components of flightgear. These components
should implement generic 'traffic' interfaces with Flightgear logic.

## platform-jme-ext
Extension of platform-jme as a helper for faster dev cycles. In principle
everything in project "tcp-flightgear" should be usable as plugin in "tcp-22".

# Credits

Syd Adams, Justin Smithies for the 777.

Stewart Andreason (http://seahorseCorral.org/flightgear_aircraft.html) for 'bluebird'

# Implementation Details

## Model
The preferred model format in Flightgear is 'ac'. This is human readable for the
price of high system load at runtime. For saving resources, ac-files are converted
to gltf files during bundle building. Unfortunately the converter still has bugs,
eg. some special ac features like two sided faces are not yet converted
correctly.