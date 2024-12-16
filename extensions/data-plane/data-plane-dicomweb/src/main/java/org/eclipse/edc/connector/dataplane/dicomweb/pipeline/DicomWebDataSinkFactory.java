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
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSink;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSinkFactory;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

import static org.eclipse.edc.connector.dataplane.dicomweb.metadata.DicomWebSchema.PASSWORD;
import static org.eclipse.edc.connector.dataplane.dicomweb.metadata.DicomWebSchema.URL;
import static org.eclipse.edc.connector.dataplane.dicomweb.metadata.DicomWebSchema.USERNAME;

/**
 * Instantiates {@link DicomWebDataSink}s for requests whose destination data type is {@link DicomWebSchema#TYPE}.
 */
public class DicomWebDataSinkFactory implements DataSinkFactory {

    private final DicomWebClient dicomWebClient;
    private final Monitor monitor;
    private final Vault vault;
    private final ExecutorService executorService;

    public DicomWebDataSinkFactory(DicomWebClient dicomWebClient, Monitor monitor, Vault vault, ExecutorService executorService) {
        this.dicomWebClient = dicomWebClient;
        this.monitor = monitor;
        this.vault = vault;
        this.executorService = executorService;
    }

    @Override
    public String supportedType() {
        return DicomWebSchema.TYPE;
    }

    @Override
    public boolean canHandle(DataFlowStartMessage request) {
        return DicomWebSchema.TYPE.equals(request.getDestinationDataAddress().getType());
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage request) {
        var dataAddress = request.getDestinationDataAddress();
        try {
            // Implement some logic here
            if (dataAddress == null) {
                return Result.failure("Data address is null");
            }
            createSink(request);
        } catch (Exception e) {
            return Result.failure("Failed to build DicomWebDataSink: " + e.getMessage());
        }
        return Result.success();
    }

    @Override
    public DataSink createSink(DataFlowStartMessage request) {
        var dataAddress = request.getDestinationDataAddress();
        var builder = DicomWebDataSink.Builder.newInstance()
                .dicomWebClient(dicomWebClient)
                .monitor(monitor)
                .url(dataAddress.getStringProperty(URL))
                .username(dataAddress.getStringProperty(USERNAME))
                .password(dataAddress.getStringProperty(PASSWORD))
                .requestId(request.getId())
                .executorService(executorService)
                .build();
        if (builder == null) {
            throw new IllegalStateException("Failed to build DicomWebDataSink");
        }
        return builder;
    }
}