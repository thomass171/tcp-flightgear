This directory 'fg-raw-data' contains original FG data files like scenery tiles, models, etc.

These files are needed for unit testing and for running the scenes. Some of the files are
just copied to a bundle, others are converted additionally.

The GLTF files in here use the traditional pre 2024 format with multiple
primitives per mesh. The files are kept as reference for now.

# Content

## bluebird
By Stewart Andreason (http://seahorseCorral.org/flightgear_aircraft.html).

Files are from appx. 2019. All files are unchanged.

## fgdatabasic

A subset of the full 'fgdatabasic' bundle. Using the same name is confusing,
but using 'fgdatabasic-light' is difficult because of currently hardcoded usage 
during init. Maybe later.

Contains yoke and pedals.

## sgmaterial

A subset of the full 'sgmaterial' bundle.

## terrasync

A subset of the full 'TerraSync' bundle.