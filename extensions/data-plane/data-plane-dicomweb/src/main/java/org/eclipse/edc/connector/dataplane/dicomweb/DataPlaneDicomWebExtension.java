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

package org.eclipse.edc.connector.dataplane.dicomweb;

import org.eclipse.edc.connector.dataplane.dicomweb.pipeline.DicomWebClient;
import org.eclipse.edc.connector.dataplane.dicomweb.pipeline.DicomWebDataSinkFactory;
import org.eclipse.edc.connector.dataplane.dicomweb.pipeline.DicomWebDataSourceFactory;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataTransferExecutorServiceContainer;
import org.eclipse.edc.connector.dataplane.spi.pipeline.PipelineService;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

/**
 * Provides support for reading data from a DICOM server and sending data to a PACS system using DICOMweb.
 */
@Extension(value = DataPlaneDicomWebExtension.NAME)
public class DataPlaneDicomWebExtension implements ServiceExtension {
    public static final String NAME = "Data Plane DicomWeb";

    @Inject
    private EdcHttpClient httpClient;

    @Inject
    private PipelineService pipelineService;

    @Inject
    private DataTransferExecutorServiceContainer executorContainer;

    @Inject
    private Vault vault;

    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        var dicomWebClient = new DicomWebClient(httpClient, monitor);
        var sourceFactory = new DicomWebDataSourceFactory(dicomWebClient, monitor, vault);
        pipelineService.registerFactory(sourceFactory);

        var sinkFactory = new DicomWebDataSinkFactory(dicomWebClient, monitor, vault, executorContainer.getExecutorService());
        pipelineService.registerFactory(sinkFactory);

    }

}
