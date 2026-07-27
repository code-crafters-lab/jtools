deploy:
	./gc-excel-lic9 deploy -g 6064-1107-2722-0291-082 req.txt

offline:
	./gc-excel-lic9 deploy -i offlineActivationCode.txt

demo:
	@export GCEXCEL_JAVA_DEPLOY_LICENSE_V9=$$(cat src/test/resources/v9.lic); \
	export GCEXCEL_DEMO_OPTS=-javaagent:../jagent/distribution/build/dist/jagent.jar; \
	build/install/gcexcel-demo/bin/gcexcel-demo
