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
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.eclipse.edc.connector.dataplane.spi.pipeline.StreamResult.error;
import static org.eclipse.edc.connector.dataplane.spi.pipeline.StreamResult.success;


/**
 * A data source that reads DICOM data from a DICOMweb endpoint.
 */
public class DicomWebDataSource implements DataSource {
    private String name;
    private String url;
    private String username;
    private String password;
    private String requestId;
    private Monitor monitor;
    private DicomWebClient dicomWebClient;
    private final AtomicReference<ResponseBodyStream> responseBodyStream = new AtomicReference<>();

    private DicomWebDataSource() {
    }

    @Override
    public StreamResult<Stream<DataSource.Part>> openPartStream() {
        monitor.debug(() -> "Executing PACS request: " + url);
        try {
            var result = dicomWebClient.wadoRs(url, username, password);
            if (result.succeeded()) {
                var dicomDataList = result.getContent();
                var parts = dicomDataList.stream()
                        .map(data -> new DicomWebPart(name, new ByteArrayInputStream(data), "application/dicom"))
                        .map(part -> (DataSource.Part) part) // Cast to DataSource.Part
                        .toList();
                return success(parts.stream());
            } else {
                return error("Failed to retrieve data from PACS: " + result.getFailureDetail());
            }
        } catch (Exception e) {
            throw new EdcException(e);
        }
    }

    @Override
    public void close() {
        var bodyStream = responseBodyStream.get();
        if (bodyStream != null) {
            try {
                bodyStream.stream().close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    private record ResponseBodyStream(byte[] responseBody, InputStream stream) {
    }

    public static class Builder {
        private final DicomWebDataSource dataSource;

        public static Builder newInstance() {
            return new Builder();
        }

        private Builder() {
            dataSource = new DicomWebDataSource();
        }

        public Builder url(String url) {
            dataSource.url = url;
            return this;
        }

        public Builder username(String username) {
            dataSource.username = username;
            return this;
        }

        public Builder password(String password) {
            dataSource.password = password;
            return this;
        }

        public Builder name(String name) {
            dataSource.name = name;
            return this;
        }

        public Builder requestId(String requestId) {
            dataSource.requestId = requestId;
            return this;
        }

        public Builder dicomWebClient(DicomWebClient dicomWebClient) {
            dataSource.dicomWebClient = dicomWebClient;
            return this;
        }

        public Builder monitor(Monitor monitor) {
            dataSource.monitor = monitor;
            return this;
        }

        public DicomWebDataSource build() {
            Objects.requireNonNull(dataSource.requestId, "requestId");
            Objects.requireNonNull(dataSource.dicomWebClient, "dicomWebClient");
            Objects.requireNonNull(dataSource.monitor, "monitor");
            return dataSource;
        }
    }

    public record DicomWebPart(String name, InputStream content, String mediaType) implements Part {

        @Override
        public long size() {
            return SIZE_UNKNOWN;
        }

        @Override
        public InputStream openStream() {
            return content;
        }

        @Override
        public String mediaType() {
            return mediaType;
        }
    }
}