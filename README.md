# DICOMWeb Data Transfer Extension for Eclipse Dataspace Components (EDC) Connectors

This extension provides the capability to fetch or post data from/to a DICOMWeb endpoint when executing a data transfer scenario with [EDC](https://github.com/eclipse-edc) Connectors.

> Note: This extension is a sample implementation of a data transfer extension for EDC Connectors. It is not intended for production use.

## Quick start

1. Clone this repository
2. Run `./gradlew clean :runtimes:connector:build` to build a sample connector
3. Run `docker run -p 4242:4242 -p 8042:8042 --rm jodogne/orthanc-plugins` to start an [Orthanc DICOMWeb server](https://www.orthanc-server.com)
4. Start a provider connector with the following command:
```shell
java -Dedc.keystore=runtimes/connector/certs/cert.pfx \
   -Dedc.keystore.password=123456 \
   -Dedc.fs.config=runtimes/connector/configuration/provider-configuration.properties \
   -jar runtimes/connector/build/libs/connector.jar
```
5. Start a consumer connector with the following command:
```shell
java -Dedc.keystore=runtimes/connector/certs/cert.pfx \
     -Dedc.keystore.password=123456 \
     -Dedc.fs.config=runtimes/connector/configuration/consumer-configuration.properties \
     -jar runtimes/connector/build/libs/connector.jar
```
6. Execute a data transfer scenario with the templates in the [runtimes/connector/resources](runtimes/connector/resources) directory

For more information on the development and usage of this extension, please refer to the [Medium article](https://medium.com/@marcelfernandez_26751/secure-and-sovereign-medical-image-sharing-via-dicomweb-and-eclipse-dataspace-components-edc-4c6abcfacc88).