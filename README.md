
This is an extension of the cross platform 3D graphics meta engine for Java in project
https://github.com/thomass171/tcp-22 with components of flightgear. Flightgear (https://www.flightgear.org) is a free
open source flight simulator written in C++ and using OSG under GPL.

Main objective is to have Flightgear scenery and aircraft models 
available in traveling scenes running in a browser (including VR).

Also this is still WiP! See also the [examples](https://thomass171.github.io/tcp-flightgear/tcp-flightgear.html) and
my [Blog](https://thomass171.github.io/blog).

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

  * simgear property tree including SGExpressions
  * Scenery loader
  * Model loader
  * Aircraft/Vehicle loader 'FgVehicleLoader'

Flightgear should be a kind of plugin to module 'traffic'.

Design of migration is:
  * Replace OSG by ...
  * Move global property trees to ...
  * TODO: BTG model loading is part of tools, so only available for converting aso.

TODO: extract a core module without engine dependency to have tools-fg without engine dependency.

#### FG animations
Animations are a key feature in flightgear for having effects.

For example Models/Power/windturbine.xml has a 'Rotate' animation
```
<type>rotate</type>
  <object-name>Generator</object-name>
  <object-name>Shaft</object-name>
  <object-name>Hub</object-name>
  <object-name>Blade1</object-name>
  <object-name>Blade2</object-name>
  <object-name>Blade3</object-name>
  <property>/environment/wind-from-heading-deg</property>
...
```
that rotates the six listed objects by wind heading and a 'Spin' animation
```
<type>spin</type>
  <object-name>Shaft</object-name>
  <object-name>Hub</object-name>
  <object-name>Blade1</object-name>
  <object-name>Blade2</object-name>
  <object-name>Blade3</object-name>
...
  <property>/environment/wind-speed-kt</property>
...  
```
that lets spin the listed objects (five of these are already used in first animation) according to wind speed.
In the [scene object tree](#scene-object-tree) special nodes (eg. SGRotateAnimation, which is also used for 'spin'
animations) are created for each animation. To these nodes
the changes of values are supplied as needed during runtime. The objects listed
in the animation definition are attached to the animation nodes accordingly.

Currently the following animations are migrated:
  * PickAnimation
  * RotateAnimation, eg. windturbine, needle in ASI

For animated scenery objects ECS entities containing a FgAnimationComponent will be
created while for vehicle animations in sub models all FgAnimationComponents
are collected in the vehicle entity.

### tools-fg

### traffic-fg
Extension of base module 'traffic' with components of flightgear. These components
should implement generic 'traffic' interfaces with Flightgear logic.

### traffic-advanced
Use cases that use the traffic implementations of 'traffic-fg'. The setup including unit
tests also uses externally hosted bundles.

### platform-jme-ext
Extension of platform-jme as a helper for faster dev cycles. In principle
everything in project "tcp-flightgear" should be usable as plugin in "tcp-22".

### platform-webgl-ext
Extension of platform-webgl.

# Credits

Wayne Bragg (and others) for c172p (TODO link).

Syd Adams, Justin Smithies for the 777 (TODO link).

Stewart Andreason (http://seahorseCorral.org/flightgear_aircraft.html) for 'bluebird'

# Flightgear data
Flightgear needs data for
  * scenery
  * scenery material
  * aircraft model
  * aircraft commons
  * ground nets

See also 'fg-raw-data/Readme.md' about data. 

## Scenery data
Flightgear scenery data is stored by 'TerraySync' in a directory tree with three sub dirs

  * 'Terrain'
  * 'Objects': Tiles like 'Terrain'. Contains tile specific model
  * 'Models': Shared model (like wind turbine)


This project contains a subset of FG tiles around EDDK for unit testing and illustrating.
The full set is available via a proxy server...

Bundle building for scenery differs from other bundles (mkTerraSyncBundle.sh). The original TerraSync
directory structure is retained and just directory files are added in the root of the tree.
So the bundles are a kind of overlay on the TerraSync structure.

The bundle for 'Models' with a dir file 'directory-model.txt' isn't used currently. And
it might be too large.

btg-files are converted to gltf files during bundle building. Even though converted to gltf, the bundle directory will still list the filename with
suffix 'btg'.

## Terrain(btg) conversion
The btg files contain a terrain mesh with a land class assigned to each
mesh element (triangle?). During rendering the land class is mapped
to a material (texture/effect) depending on the region and season.
So far so good. The idea of material definition however isn't that clear.
Probably a material/landclass name should be unique per region and season, but
probably it isn't. At least not reliable.

Since we convert the btg files to gltf in an external process (mkTerraSyncBundle.sh), we cannot consider seasons efficiently. We
could consider region, but currently don't do so. We
just use Europe/Summer.

There are some materials like

  * pc_helipad
  * SoneSort
  * pa_helipad
  * Crop
  * Shrub

that might be defined nowhere. This needs further investigation.

## Aircraft Model
This project contains the aircraft model 'bluebird' for unit testing and illustrating.

The bundle fgdatabasic (build from fg-raw-data) is a subset of the $FGROOT/Aircraft directory.

More advanced aircraft model are used in module traffic-advanced. These are loaded from an external server, which is a temporary solution
until there is a full FG proxy set up.

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

## Scene Object Tree

The scene object tree of FG is retained as far as possible. ...

FG uses proxy nodes for model that are not loaded before the viewer steps onto that
node (see SGModelLib).

## Effects
See also https://wiki.flightgear.org/Effect_framework.

In FG effects (eg. transparency) are defined in XML and applied at runtime 
via an OSG node visitor (MakeEffectVisitor).

An important step of migration is replacing OSG classes StateSet and Material with
our classes Texture and Material. Instead of having a (global) current state we need
to pass the material context to ... for applying it.

The effect property '<inherits-from>' probably just means 'copy property tree from parent'. The
parent itself is never realized.

We ignore schemes and compositor for now.

Later versions (2024) of Flightgear apparently no longer prefer using '<inherits-from>Effects/model-transparent</inherits-from>' for
effects? At least beacon.xml no longer uses it. So this is no good use/test case for
understanding how it works. Maybe it never worked? "model-transparent.eff" appears
no longer popular at all; only for scenery?
beacon in fg-raw-data replaced with latest version.
