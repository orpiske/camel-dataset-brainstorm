# Camel Data Set Brainstorm 

This is a [Brainstorm-based](https://github.com/orpiske/brainstorm) data pipeline capable of building the 
[Camel documentation dataset](https://huggingface.co/datasets/megacamelus/camel-documentation). 

This serves as a more complex data-pipeline example showing the usage of Java code along with custom containers for data
transformation.

# Pre-requisites

Make sure you have Brainstorm installed and running. 

# Building

Adjust the path of the CLI on the `Makefile`

```
CLI_HOME:=$(HOME)/code/java/brainstorm/brainstorm/cli/
```

Then, export the user and password for the container registry: 

```shell
export ORGANIZATION_USER=myuser
export REGISTRY_PASSWD=mypasswrd
```

**NOTE**: the system should ask for the password if not provided.

To build the project and deploy the containers, run: 

```make build```

To deploy it to the cluster, run:

```make deploy```

Wait until the pipeline is completed (i.e.; like in the state below):

```[~]$ k get pods
NAME                             READY   STATUS      RESTARTS   AGE
camel-dataset-brainstorm-zzq6q   0/1     Completed   0          39m
service-5669bd974d-xxnk5         1/1     Running     0          39m
step-01-h76hh                    0/1     Completed   0          39m
step-02-h2rzr                    0/1     Completed   0          39m
```

Then copy the resulting files:

```
kubectl cp service-5669bd974d-xxnk5:/opt/brainstorm/data/dataset ~/tmp/result
```
