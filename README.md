
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


## traffic-ext
Extension of base module 'traffic' with components of flightgear. These components
should implement generic 'traffic' interfaces with Flightgear logic.

# Credits

Syd Adams, Justin Smithies for the 777.