CLI_HOME:=$(HOME)/code/java/brainstorm/brainstorm/cli/
CLI_PATH=$(CLI_HOME)/target/quarkus-app/quarkus-run.jar

PROJ_PATH=$(PWD)
ARTIFACTS=$(PROJ_PATH)/code-fetcher/target/code-fetcher-jar-with-dependencies.jar
INGESTION=$(PROJ_PATH)/yaml/routes.yaml

REGISTRY:=quay.io
ORGANIZATION:=orpiske

ACQUISITION_BASE_IMAGE:=$(REGISTRY)/$(ORGANIZATION)/camel-source:latest
ACQUISITION_OUTPUT_IMAGE:=$(REGISTRY)/$(ORGANIZATION)/camel-source-runner-01:latest

TRANSFORMATION_IMG_01=$(REGISTRY)/$(ORGANIZATION)/runner-transformer-step-01
TRANSFORMATION_IMG_02=$(REGISTRY)/$(ORGANIZATION)/runner-transformer-step-02
TRANSFORMATION_IMG_03=$(REGISTRY)/$(ORGANIZATION)/camel-transformer-step-03

build:
	mvn clean package
	java -jar $(CLI_PATH) package acquisition --artifact $(ARTIFACTS) --ingestion $(INGESTION) --output-image $(ACQUISITION_OUTPUT_IMAGE) --username $(ORGANIZATION_USER) --password $(REGISTRY_PASSWD)
	java -jar $(CLI_PATH) package runner --base-dir $(PROJ_PATH)/transformation/01/ --output-image $(TRANSFORMATION_IMG_01)
	java -jar $(CLI_PATH) package runner --base-dir $(PROJ_PATH)/transformation/02/ --output-image $(TRANSFORMATION_IMG_02)
	java -jar $(CLI_PATH) package runner --base-dir $(PROJ_PATH)/transformation/03/ --output-image $(TRANSFORMATION_IMG_03)
	podman push $(TRANSFORMATION_IMG_01)
	podman push $(TRANSFORMATION_IMG_02)
	podman push $(TRANSFORMATION_IMG_03)


deploy:
	kubectl apply -f k8s/pipeline.yaml

undeploy:
	kubectl delete -f k8s/pipeline.yaml