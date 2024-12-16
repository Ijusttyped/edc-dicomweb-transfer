/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */


plugins {
    `java-library`
}

val annotationProcessorVersion: String by project
val metaModelVersion: String by project

buildscript {
    dependencies {
        val edcGradlePluginsVersion: String by project
        classpath("org.eclipse.edc.edc-build:org.eclipse.edc.edc-build.gradle.plugin:${edcGradlePluginsVersion}")
    }
}

allprojects {
    apply(plugin = "${group}.edc-build")

    // configure which version of the annotation processor to use. defaults to the same version as the plugin
    configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
        processorVersion.set(annotationProcessorVersion)
        outputDirectory.set(project.layout.buildDirectory.asFile)
    }

    configure<org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension> {
        versions {
            // override default dependency versions here
            metaModel.set(metaModelVersion)
        }
        pom {
            scmUrl.set("https://github.com/Ijusttyped/edc-dicomweb-transfer.git")
            scmConnection.set("scm:git:git@github.com:Ijusttyped/edc-dicomweb-transfer.git")
            developerName.set("Marcel Fernandez Rosas")
            developerEmail.set("marcel@fernandezrosas.com")
            projectName.set("EDC DICOMWeb Transfer")
            projectUrl.set("https://github.com/Ijusttyped/edc-dicomweb-transfer")
            description.set("EDC data plane extension for DICOM data transfer using DICOMWeb")
            licenseUrl.set("https://opensource.org/licenses/MIT")
        }
    }

}