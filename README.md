
This is an extension of the cross platform 3D graphics meta engine for Java in project
https://github.com/thomass171/tcp-22 with components of flightgear. Flightgear (https://www.flightgear.org) is a free
open source flight simulator written in C++ and using OSG under GPL.

Main objective is to have Flightgear scenery and aircraft models 
available in traveling scenes running in a browser (including VR).

Also this is still WiP!

See https://thomass171.github.io/tcp-flightgear/tcp-flightgear.html for examples.

# Building And Deploying

This shows the build and installation to a web browser running locally serving from
directory $HOME/Sites. Set shell variable HOSTDIRFG (analog to HOSTDIR in tcp-22), eg.:

```
git clone https://github.com/thomass171/tcp-flightgear.git 
export HOSTDIRFG=$HOME/Sites/tcp-flightgear
```
and create that base directory. Build and install tcp-22 to your local maven repos. The build
artifacts are needed here. 

Run the following steps either manually or use the script bin/buildAndDeploy.sh for running all.

Maven is needed for building. And bundles are needed for unit testing.
So the first build is without testing for having tools available. Run

```
mvn clean install -DskipTests=true

sh bin/deployBundles.sh
sh bin/mkTerraSyncBundle.sh    

mvn install
```

for building.

Bundles 'engine' and 'data' need to be deployed (copied) manually currently to avoid the need of ADDITIONALBUNDLE.

# Running
## Browser
Enter the URL
```
http://localhost/<youruserdir>/???
```
in your browser.

# Development
The most convenient way is to develop for a Java platform like JME (or homebrew) initially and later test it on other platforms
like ThreeJs and Unity. Thats because the other platforms need converting which reduces
roundtrip time.

In your IDE you might create a launch configuration like the following.

![](docs/IDErunConfiguration.png)

For running Java 3D software native libraries like 'lwjgl' are required.
These are typically located in the current working directory or via LD_LIBRARY_PATH.

# Technical Details
## Modules 
### flightgear
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
### tools-fg

### traffic-fg
Extension of base module 'traffic' with components of flightgear. These components
should implement generic 'traffic' interfaces with Flightgear logic.

### platform-jme-ext
Extension of platform-jme as a helper for faster dev cycles. In principle
everything in project "tcp-flightgear" should be usable as plugin in "tcp-22".

# Credits

Syd Adams, Justin Smithies for the 777.

Stewart Andreason (http://seahorseCorral.org/flightgear_aircraft.html) for 'bluebird'

# Flightgear data
Flightgear needs data for
  * scenery
  * scenery material
  * aircraft model
  * aircraft commons
  * ground nets

## Scenery data
Flightgear scenery data is stored by 'TerraySync' in a directory tree.
This project contains a subset of FG tiles around EDDK for unit testing and illustrating.
The full set is available via a proxy server...

Bundle building for scenery differs from other bundles (mkTerraSyncBundle.sh). The original TerraSync
directory structure is retained and just directory files are added in the root of the tree.
So the bundles are a kind of overlay on the TerraSync structure.

btg-files are converted to gltf files during bundle building. Even though converted to gltf, the bundle directory will still list the filename with
suffix 'btg'.

## Aircraft Model
This project contains the aircraft model 'bluebird' for unit testing and illustrating.
More advanced aircraft model are available via a proxy server...

# Implementation Details

## Model
The preferred model format in Flightgear is 'ac'. This is human readable for the
price of high system load at runtime. For saving resources, ac-files are converted
to gltf files during bundle building. Unfortunately the converter still has bugs,
eg. some special ac features like two sided faces are not yet converted
correctly.

Even though converted to gltf, the bundle directory will still list the filename with
suffix 'ac'.

FG uses XML configuration for aircraft model and has two specific ways of component lookup:
* Some aircraft reference components in some global directory ($FG_ROOT/aircraft(?) in FG).
* Some aircraft reference components in their own directory/bundle by using a prefix "Aircraft", that isn't part of any path.

The AircraftResourceProvider helps finding these components independent from a specific installation directory and bundle.
Eg. 'bluebirds' 'YOKE' references 'Aircraft/bluebird/Instruments-3d/yoke/yoke.xml', which
could be found in either FG_ROOT/Aircraft or in bluebirds own directory
with prefix 'Aircraft' removed.
A 'Aircraft/Instruments-3d/yoke/yoke.ac' path however points to FG_ROOT.
