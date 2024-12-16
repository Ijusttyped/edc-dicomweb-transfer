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

import org.eclipse.edc.connector.dataplane.dicomweb.metadata.DicomWebSchema;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.edc.connector.dataplane.dicomweb.metadata.DicomWebSchema.PASSWORD;
import static org.eclipse.edc.connector.dataplane.dicomweb.metadata.DicomWebSchema.URL;
import static org.eclipse.edc.connector.dataplane.dicomweb.metadata.DicomWebSchema.USERNAME;


/**
 * Instantiates {@link DicomWebDataSource}s for requests whose source data type is {@link DicomWebSchema#TYPE}.
 */
public class DicomWebDataSourceFactory implements DataSourceFactory {

    private final DicomWebClient dicomWebClient;
    private final Monitor monitor;
    private final Vault vault;

    public DicomWebDataSourceFactory(DicomWebClient dicomWebClient, Monitor monitor, Vault vault) {
        this.dicomWebClient = dicomWebClient;
        this.monitor = monitor;
        this.vault = vault;
    }

    @Override
    public String supportedType() {
        return DicomWebSchema.TYPE;
    }

    @Override
    public boolean canHandle(DataFlowStartMessage request) {
        return DicomWebSchema.TYPE.equals(request.getSourceDataAddress().getType());
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage request) {
        var dataAddress = request.getSourceDataAddress();
        try {
            // Implement some logic here
            if (dataAddress == null) {
                return Result.failure("Data address is null");
            }
            createSource(request);
        } catch (Exception e) {
            return Result.failure("Failed to build PacsDataSource: " + e.getMessage());
        }
        return Result.success();
    }

    @Override
    public DataSource createSource(DataFlowStartMessage request) {
        var dataAddress = request.getSourceDataAddress();
        var builder = DicomWebDataSource.Builder.newInstance()
                .dicomWebClient(dicomWebClient)
                .monitor(monitor)
                .requestId(request.getId())
                .name(dataAddress.getStringProperty("name", "DicomWebDataSource"))
                .url(dataAddress.getStringProperty(URL))
                .username(dataAddress.getStringProperty(USERNAME))
                .password(dataAddress.getStringProperty(PASSWORD))
                .build();
        if (builder == null) {
            throw new IllegalStateException("Failed to build DicomWebDataSource");
        }
        return builder;
    }
}