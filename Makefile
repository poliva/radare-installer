BUILD=debug
#BUILD=release

build: clean
	ant ${BUILD}

clean:
	rm -rf bin gen

uninstall:
	adb shell 'LD_LIBRARY_PATH=/system/lib pm uninstall org.radare.installer'


install: uninstall build
	adb install bin/radare2\ installer-${BUILD}.apk
	adb shell 'LD_LIBRARY_PATH=/system/lib am start -n org.radare.installer/.LaunchActivity'

test:
	adb shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall org.radare.installer'"
	#adb shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall jackpal.androidterm'"
	rm -rf bin gen
	ant ${BUILD} install
	adb shell rm /sdcard/radare2\ installer-${BUILD}.apk
	adb push bin/radare2\ installer-${BUILD}.apk /sdcard/
	adb shell "su -c 'LD_LIBRARY_PATH=/system/lib pm install /sdcard/radare2\ installer-${BUILD}.apk'"
	adb shell 'LD_LIBRARY_PATH=/system/lib am start -n org.radare.installer/.LaunchActivity'
	

