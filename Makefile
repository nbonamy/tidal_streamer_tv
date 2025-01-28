
build: FORCE
	@echo
	@echo '  *******'
	@echo '   BUILD'
	@echo '  *******'
	@echo
	@mkdir -p ./release
	./gradlew app:assembleRelease
	@cp ./app/build/outputs/apk/release/app-release.apk ./release/

deploy: build
	@echo
	@echo '  ********'
	@echo '   DEPLOY'
	@echo '  ********'
	@echo
	@adb connect 192.168.1.4 > /dev/null
	adb -s 192.168.1.4 install ./release/app-release.apk
	#@adb connect 192.168.1.10 > /dev/null
	#adb -s 192.168.1.10 install ./release/app-release.apk

FORCE:
