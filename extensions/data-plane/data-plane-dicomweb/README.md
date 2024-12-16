# Data Plane DICOMWeb extension

This extension provides the capability to fetch or post data from/to a DICOMWeb endpoint.

## Data Address Configuration

The type of the data address configuration for the DICOMWeb extension is `DicomWebData`.

The data address configuration for the DICOMWeb extension is a URL that points to a DICOMWeb endpoint.
The URL should be in the format `http[s]://<host>:<port>/<path>`. The path should be the root path of the DICOMWeb endpoint.

The data address additionally expects the username and password for the DICOMWeb endpoint.

An example of a data address configuration for the DICOMWeb extension is as follows:

```json
  "dataAddress": {
    "type": "DicomWebData",
    "name": "DICOM image of lung scan",
    "url": "http://localhost:8042/dicom-web/studies/{StudyInstanceUID}",
    "username": "orthanc",
    "password": "orthanc"
  }
```