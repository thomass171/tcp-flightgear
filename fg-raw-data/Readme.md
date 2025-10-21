This directory 'fg-raw-data' contains original FG data files like scenery tiles, models, etc.

These files are needed for unit testing and for running the scenes. Some of the files are
just copied to a bundle, others are converted additionally.

The GLTF files in here use the traditional pre 2024 format with multiple
primitives per mesh. The files are kept as reference for now.

# Content

## bluebird
By Stewart Andreason (http://seahorseCorral.org/flightgear_aircraft.html).

Files are from appx. 2019. All files are unchanged.

But some effect/model files were added for testing and more extended
cockpit. In 2024 Garmin was added to the cockpit.

Originally 'bluebird' has no real effect
file (with suffix 'eff'). So all 'eff' files were added for testing. These are
(or should be) added to the cockpit of 'bluebird'.

Added from c172p (the 2024 converted):
  * .../mag-compass (uses Aircraft references)
  * .../digital-clock
  * .../kx165
  * interior/procedural-light-dome.eff
  * Effects/interior/lm-mag.eff
  * Effects/interior/lm-magr.eff
  * Effects/interior/lm_mag.png
  * Effects/interior/interior-glass-reflection-panel-front.eff

## fgdatabasic

A subset of the full 'fgdatabasic' bundle. Using the same name is confusing,
but using 'fgdatabasic-light' is difficult because of currently hardcoded usage 
during init. Maybe later.

Contains yoke and pedals.

## sgmaterial

A subset of the full 'sgmaterial' bundle.

## terrasync

A subset of the full 'TerraSync' bundle.