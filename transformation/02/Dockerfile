#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
FROM quay.io/bstorm/runner-worker:latest as build
LABEL maintainer="orpiske@apache.org"
WORKDIR /opt/brainstorm/build
RUN dnf install -y java-21-devel git maven && \
    git clone https://github.com/megacamelus/camel-jbang-plugin-explain.git && \
    cd camel-jbang-plugin-explain/ && \
    mvn -Pstandalone package && \
    dnf clean all

FROM quay.io/bstorm/runner-worker:latest as base
WORKDIR /opt/brainstorm/step/
COPY --chmod=755 transform.sh ./
COPY --from=build /opt/brainstorm/build/camel-jbang-plugin-explain/target/camel-jbang-plugin-explain-4.8.0-jar-with-dependencies.jar ./