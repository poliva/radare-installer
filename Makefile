BUILD=debug
#BUILD=release

clean:
	rm -rf bin gen

uninstall:
	adb shell 'LD_LIBRARY_PATH=/system/lib pm uninstall org.radare.installer'

build: clean
	ant ${BUILD}

install: uninstall build
	adb shell 'LD_LIBRARY_PATH=/system/lib am start -n org.radare.installer/.MainActivity'

test:
	adb shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall org.radare.installer'"
	#adb shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall jackpal.androidterm'"
	rm -rf bin gen
	ant ${BUILD} install
	adb shell rm /sdcard/radare2\ installer-${BUILD}.apk
	adb push bin/radare2\ installer-${BUILD}.apk /sdcard/
	adb shell "su -c 'LD_LIBRARY_PATH=/system/lib pm install /sdcard/radare2\ installer-${BUILD}.apk'"
	adb shell 'LD_LIBRARY_PATH=/system/lib am start -n org.radare.installer/.MainActivity'
	

