#!/bin/sh

echo
echo "Applying patch..."
echo

current_dir=$(basename "$PWD")

if [ -d "classical-components-demo" ] && [ -d "ready-to-use-ui-demo" ]; then
    find . -name "build.gradle" -type f -exec sed -i '' 's/minifyEnabled = false/minifyEnabled = true/g' {} \;
elif [ "$current_dir" == "classical-components-demo" ] || [ "$current_dir" == "ready-to-use-ui-demo" ]; then
	cd ..
	find . -name "build.gradle" -type f -exec sed -i '' 's/minifyEnabled = false/minifyEnabled = true/g' {} \;
else
    echo "You are trying to run this script from wrong folder! Go to 'scanbot-sdk-example-android' project's root and try again!"
fi

echo
echo "DONE!"
echo
