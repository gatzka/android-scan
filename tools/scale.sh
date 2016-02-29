#!/bin/bash

res=(
  'drawable-xxxhdpi=224'
  'drawable-xxhdpi=168'
  'drawable-xhdpi=112'
  'drawable-hdpi=84'
  'drawable-mdpi=56'
)

regex="(.*)=([[:digit:]]+)"
for element in "${res[@]}"; do
  [[ $element =~ $regex ]]
  mkdir -p ${BASH_REMATCH[1]}
  convert $1[0] -resize ${BASH_REMATCH[2]}x${BASH_REMATCH[2]} -gravity center -extent ${BASH_REMATCH[2]}x${BASH_REMATCH[2]} -fuzz 5% -transparent white ${BASH_REMATCH[1]}/$2
  exiftool -overwrite_original -all= ${BASH_REMATCH[1]}/$2
done
