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

package org.eclipse.edc.connector.dataplane.dicomweb.metadata;

/**
 * Constants used in DICOMWeb data address properties.
 */
public class DicomWebSchema {

    public static final String TYPE = "DicomWebData";
    public static final String TRANSFERTYPE_PUSH = TYPE + "-PUSH";
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
}
