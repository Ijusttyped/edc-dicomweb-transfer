/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Marcel Fernandez Rosas - modification for the DICOMweb data plane
 *
 */
plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(libs.edc.spi.dataplane)
    api(libs.edc.spi.http)
    implementation(libs.edc.core.dataPlane.util)
    implementation("com.sun.mail:javax.mail:1.6.2")
}