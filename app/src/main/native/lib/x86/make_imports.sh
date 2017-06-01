#!/usr/bin/env bash

set -ue

ANDROID_ABI='${ANDROID_ABI}'

parse_filename() {
	echo $1 | perl -ne '/_(\w+)\.(\w+)$/ && print "$1 $2\n"'
}

for file in *; do
	read module_name file_type <<< $(parse_filename $file)

	if [[ "$file_type" == so ]]; then
		lib_type=SHARED
	else
		lib_type=STATIC
	fi

	cat <<HERE
add_library(
    opencv-${module_name} ${lib_type} IMPORTED
)
set_target_properties(
    opencv-${module_name} PROPERTIES IMPORTED_LOCATION src/main/jni/lib/${ANDROID_ABI}/libopencv_${module_name}.${file_type}
)
HERE
done
