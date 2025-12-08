
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
sh bin/deploy.sh
```

for building.

Bundles 'engine' and 'data' need to be deployed (copied) manually currently to avoid the need of ADDITIONALBUNDLE.

# Running
## Browser
Enter the URL
```
http://localhost/<youruserdir>/tcp-flightgear/tcp-flightgear.html?host=http://localhost/<youruserdir>/tcp-flightgear
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

After applying all animations the node graph in FG looks like (wind 270° at 3 kt)

```
-windturbine.ac
 +-rotate animation (angle=-360.002, axis=[ 0, 0, 1 ])
   +-spin rotate animation
     +-Blade1
   
```

The typical scene node tree layout of a XML loaded model before applying animations is

```
-xmlResource
 +-ACPolicy(2)
   +-GLTF(2)
     +-ac-world
       +-Generator
       +-Shaft
...
```

After applying animations it is (30.11.24 really? no rotate missing?)

```
-xmlResource
 +-ACPolicy(2)
   +-GLTF(2)
     +-ac-world
       +-Generator
       +-Shaft
...
```




Like for effects it's really hard to comprehend how FG implements animations. Only the base idea
(from https://wiki.flightgear.org/Howto:Animate_models) appears  
clear. 

The animationGroup/installInGroup part is FG/OSG magic. The
method installInGroup() is called many many many times. It seems that
the full (sub)model tree is traversed again and agin for finding the
node where to inject an animation group. The number of animation groups
that needs to be created for an animation depends on the current tree.
Only for animated nodes that have the same parent an animation group
can be shared. And finally FG/OSG might end up with model nodes that have
more than one parent (eg. HR.001 in didgital-clock). Strange, and not possible for us to do.

One important issue in rotate animations is the 3D way to always rotate an object around its center. FG however
provides an option to define a center for rotation. For doing this a sequence of three 3D operations is needed:
"move center","rotate","move center back" (we call it the MCRMCB pattern). FG can apply these operations
in one step to the model matrix in class SGRotateTransform, but we have no access to the final matrix. So
we embed one single FG "rotate group" into a "move center" and a "move center back" node. 

The windsock model uses cascade rotations. According to logging the final node tree for windsock_lit in FG is

```
-windsock_lit.ac
 +-rotate animation()
   +-windsock
     +-scale animation
       +-translate animation
         +-rotate animation142(angle=30, axis=[ -1, 0, 0 ])
           +-5kt
           +-rotate animation()
             +-rotate animation
               +-rotate animation
                 +-15kt
                   +-??
                 
```
Apparently each time an object is mentioned in an "animation" tag (in this cae '15kt'), a new 
animation node is inserted between the object and the current parent,
resulting in the above order (scale, translate, 4xrotate). But where did the 5th go? Maybe a FG logging problem.
An additional issue with the windsock model are windsock specific properties like '/environment/windsock/wind-speed-12.5kt'. It
appears the windsock model is a kind of hack.

So we try to implement the idea hopefully meeting what FG meets.

In the [scene object tree](#scene-object-tree) special nodes (eg. SGRotateAnimation, which is also used for 'spin'
animations) are created for each animation. To these nodes
the changes of values are supplied as needed during runtime. The objects listed
in the animation definition are attached to the animation nodes accordingly.


For updating the animations FG uses OSG NodeCallbacks (eg. UpdateCallback) that hook into OSG and are executed while rendering the
scene graph.

Currently the following animations are migrated:
  * PickAnimation
  * RotateAnimation, eg. windturbine, needle in ASI
  * SGTexTransformAnimation. FG uses ancient OpenGls TextureMatrix for fixed pipelines. Meanwhile this should be done in shader.

For animated scenery objects ECS entities containing a FgAnimationComponent will be
created while for vehicle animations in sub models all FgAnimationComponents
are collected in the vehicle entity.

#### Nasal

Using bison or antlr4 for parser generating appears risky because it is unclear
whether the Nasal language can be parsed by these (especially bison). And there are some
very special features, eg. "compile()".

compile() is used in 
  * canvas/Mapstructure
  * FG1000
  * GTX328 of ec130

but not in 777, bluebird, ec135, SpaceShuttle. c172p only for "SelectableInterfaces".

So it's probably more efficient to migrate the C implementation of the parser.

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

# Releases
This is the release list In terms of installations to the examples hosting
server used from https://thomass171.github.io/tcp-flightgear/tcp-flightgear.html. Every release contains lots of bug fixes.
Only major changes are listed here.

## 2021-06(?)
Initial release
## 2025-09
* FG 2024.1 upgrade
## 2025-11
* Switch to FGs 2024.1 c172p

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

By default, the BTG to GLTF conversion uses SGMaterialLib and picks just one
Europe/Summer texture for adding a simple texture material to the GLTF material
list. This results in a valid GLTF file that can be viewed with any GLTF viewer.
As of 11/2024 land classes are retained as material name. So during terrain building,
the FG region/season mapping can still be used.

As of 11/2024 BTG to GLTF conversion will always run with SGMaterialLib so there will
be no longer GLTF containing no material list. 

There are some materials (land classes) like

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

## BundlePool

This project only contains a subset of FG data used for unit testing 
and for running the demo scenes in 'traffic-fg'. For a better
user experience the scenes in 'traffic-advanced' use 
externally hosted bundles, to which the phrase "bundlepool" refers.


# Implementation Details

## Global Property Tree
Initially there was the idea for having a vehicle specific property tree
to allow for multiple concurrent vehicles. But it might be hard/impossible
to reliably resolve an ambicious property. So to keep it simple
we give up that idea at least for the near future 
(See also FlightGearProperties.java and FgVehicleLoader.java).

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
See also https://wiki.flightgear.org/Effect_framework. From https://wiki.flightgear.org/Effects:

> An effect is a container for a series of techniques which are all the possible things you could do with some object. The <predicate> section inside each effect determines which of the techniques we actually run. Typically the predicate section contains conditionals on a) rendering framework properties b) OpenGL extension support and c) quality level properties. The renderer searches the effects from lowest to highest technique number and uses the first one that fits the bill (which is why high quality levels precede low quality levels). The rendering itself is specified as <pass> - each pass runs the renderer over the whole scene. We may sometimes make a quick first pass to fill the depth buffer or a stencil buffer, but usually we render with a single pass. Techniques may not involve a shader at all, for instance 12 in terrain-default is a pure fixed pipeline fallback technique, but all the rendering you're interested in use a shader, which means they have a <program> section which determines what actual code is supposed to run.

Effects (eg. transparency) are defined in XML and applied at runtime 
via an OSG node visitor (MakeEffectVisitor). When the visitor is reached the current OSG
and/or OpenGL state is available via the super class osg::StateSet of class Pass and 
provides the resources (eg. textures, buffer, uvs, shader) where effects should apply. Maybe
this is done immediately before the draw command. It's unclear whether and how
resources like textures and shader are shared. It's also unclear how and when FG decides
which technique to use if multiple are available, and how and when the validness of
a technique is used.

An important step of migration is replacing OSG classes StateSet and Material with
our classes Texture and Material. Instead of having a (global) current state we provide
the material where effects/techniques should apply via material wrapper as super class of class Pass.
Typically a model loader (eg. GLTF) already created a material (texture/color) to which the effect should
apply (available by the material wrapper). It's probably no option to
build a new material based on the base material for each valid technique
because the base information (PortablMaterial) will not be available at
the time when the effect is built.

To avoid break the existing material, just the first valid technique of an effect 
will be applied to the existing material after realizeTechniques(). 


For texture sharing we still have the simple cache from tcp22.

The effect property '<inherits-from>' probably just means 'copy property tree from parent'. The
parent itself is never realized.

We ignore schemes and compositor for now.

Later versions (2024) of beacon.xml apparently no longer uses
'<inherits-from>Effects/model-transparent</inherits-from>' for
effects. So this is no good use/test case for
understanding how it works. Maybe it never worked as intended in beacon? "model-transparent.eff" appears
no longer popular at all. But c172p propeller and scenery models still use it.
beacon.xml in fg-raw-data replaced with latest version, but we keep the pre2024 version for testing.

The property tree layout for effects looks like

```
...
-parameters
 +-texture
   +-image
...
```

For terrain effects the material definitions are converted to the above layout
in SGMaterial.buildEffectProperties(). Eg. global-summer.xml defines "<texture>Terrain/dry_pasture4.png</texture>" in a material.

### Effect Caching
FG apparently has two caches for effects, effectMap in makeEffect (local or global?) and 'Cache' in Effect.hxx.
> Support for a cache of effects that inherit from this one, so
> Effect objects with the same parameters and techniques can be
> shared.

And every MakeEffectVisitor has its own effectMap. This is confusing. When an Effect object is instanciated, caches need to be adjusted. But this is apparently not done for 'effectMap'.

### Effect overview

| Effect                                                | Parent                                           | Remarks                                                                                |
|-------------------------------------------------------|--------------------------------------------------|----------------------------------------------------------------------------------------|
| Effects/model-default                                 | -                                                |                                                                                        |
| Effects/model-combined                                | Effects/model-default                            | many effect textures, 3 techniques (4,7,9)                                             |
| Effects/model-combined-deferred                       | Effects/model-combined                           | Legacy Effect for Project Rembrandt. Just inherit, no technique, no parameter; nothing |
| Aircraft/c172p/Models/ Effects/exterior/bumpspec      | Effects/model-combined-deferred                  | 3 techniques (4,7,9) , replacing those of grandparent?                                 |
| Aircraft/c172p/Models/ Effects/exterior/bumpspec-wing | Aircraft/c172p/Models/ Effects/exterior/bumpspec | only adds texture 'wing-normal.png'                                                    |



## Scenery

FG apparently does nothing when a tile is not found and will just have 'water'. Not sure how that is
implemented. For now we just create a green plane as dummy tile when a tile is not found.

## Flight Dynamics Model (FDM) 
The FDM is the base for simulating flying in FG (https://wiki.flightgear.org/Flight_Dynamics_Model). Roughly it calculates
resulting properties like speed from base properties like thrust. So the chain is like

throttle->rpm->speed

Since we do not have a flight simulator but want to integrate the FG vehicles
into the idea of 'traffic', where the user just controls the speed of
a vehicle the control chain for properties is inverted like

speed->rpm

For the same reason we don't have an 'autostart' like FG. Just speeding up
suffices for making a vehicle 'alive' by updating the related properties (in class FgAnimationComponent)
according to current speed.
