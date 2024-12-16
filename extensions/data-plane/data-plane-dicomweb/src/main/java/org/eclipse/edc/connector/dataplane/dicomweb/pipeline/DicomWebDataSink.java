/*
 *  Copyright (c) 2024 Marcel Fernandez Rosas
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Marcel Fernandez Rosas - initial implementation
 *
 */

package org.eclipse.edc.connector.dataplane.dicomweb.pipeline;

import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.StreamResult;
import org.eclipse.edc.connector.dataplane.util.sink.ParallelSink;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

/**
 * A sink that writes DICOM data to a DICOMweb endpoint.
 */
public class DicomWebDataSink extends ParallelSink {
    private static final StreamResult<Object> ERROR_WRITING_DATA = StreamResult.error("Error writing data");

    private String url;
    private String username;
    private String password;
    private DicomWebClient dicomWebClient;
    private Monitor monitor;

    @Override
    protected StreamResult<Object> transferParts(List<DataSource.Part> parts) {
        List<byte[]> dicomDataList = new ArrayList<>();
        for (var part : parts) {
            try (var inputStream = part.openStream()) {
                byte[] data = inputStream.readAllBytes();
                dicomDataList.add(data);
            } catch (Exception e) {
                monitor.severe(format("Error reading DICOM data %s", part.name()), e);
                return ERROR_WRITING_DATA;
            }
        }

        var result = dicomWebClient.stowRs(url, username, password, dicomDataList);
        if (!result.succeeded()) {
            monitor.severe(format("Error writing DICOM data to endpoint %s: %s", url, result.getFailureDetail()));
            return ERROR_WRITING_DATA;
        }

        return StreamResult.success();
    }

    private DicomWebDataSink() {
    }

    public static class Builder extends ParallelSink.Builder<Builder, DicomWebDataSink> {

        public static Builder newInstance() {
            return new Builder();
        }

        private Builder() {
            super(new DicomWebDataSink());
        }

        public Builder url(String url) {
            sink.url = url;
            return this;
        }

        public Builder username(String username) {
            sink.username = username;
            return this;
        }

        public Builder password(String password) {
            sink.password = password;
            return this;
        }

        public Builder dicomWebClient(DicomWebClient dicomWebClient) {
            sink.dicomWebClient = dicomWebClient;
            return this;
        }

        public Builder monitor(Monitor monitor) {
            sink.monitor = monitor;
            return this;
        }

        @Override
        protected void validate() {
            Objects.requireNonNull(sink.url, "url");
            Objects.requireNonNull(sink.username, "username");
            Objects.requireNonNull(sink.password, "password");
            Objects.requireNonNull(sink.dicomWebClient, "dicomWebClient");
            Objects.requireNonNull(sink.monitor, "monitor");
        }
    }
}