#
# Only extensions of tcp-22/bin/common.sh
#
export VERBOSELOG=0

export CLASSPATH




validateHOSTDIRFG() {
  if [ -z "$HOSTDIRFG" ]; then
    error "HOSTDIRFG not set"
  fi
  if [ ! -d "$HOSTDIRFG" ]; then
    error "HOSTDIRFG $HOSTDIRFG not found"
  fi
}

#
# Convert a ?? file to GLTF
#
preprocessGLTF() {
	echo "Converting $1 to GLTF"
	#18.4.17 leaves destdir!cd $GRANADADIR/desktop
	java -Djava.awt.headless=true de.yard.threed.tools.GltfProcessor -gltf -o $2 "$1"
	return $?
}
