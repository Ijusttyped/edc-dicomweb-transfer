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

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static java.lang.String.format;

/**
 * A client for interacting with a DICOMweb server.
 */
public class DicomWebClient {
    private final EdcHttpClient httpClient;
    private final Monitor monitor;

    public DicomWebClient(EdcHttpClient httpClient, Monitor monitor) {
        this.httpClient = httpClient;
        this.monitor = monitor;
    }

    public Result<String> stowRs(String url, String username, String password, List<byte[]> dicomDataList) {
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        String boundary = UUID.randomUUID().toString();
        MediaType mediaType = MediaType.parse("application/dicom");

        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder(boundary)
                .setType(MediaType.parse("multipart/related; type=\"application/dicom\"; boundary=" + boundary));

        for (int i = 0; i < dicomDataList.size(); i++) {
            byte[] data = dicomDataList.get(i);
            RequestBody fileBody = RequestBody.create(data, mediaType);
            requestBodyBuilder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"dicomfile-" + (i + 1) + ".dcm\""),
                    fileBody
            );
        }

        RequestBody requestBody = requestBodyBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "multipart/related; type=application/dicom; boundary=" + boundary)
                .header("Accept", "application/json")
                .post(requestBody)
                .build();

        try (Response response = httpClient.execute(request)) {
            if (response.isSuccessful()) {
                monitor.debug(format("HTTP request to %s was successful with status code %d and message %s",
                        url, response.code(), response.message()));
                return Result.success(response.body().string());
            } else {
                return Result.failure("Failed to upload file: " + response.message());
            }
        } catch (IOException e) {
            return Result.failure("IOException occurred during HTTP request: " + e.getMessage());
        }
    }

    public Result<List<byte[]>> wadoRs(String url, String username, String password) {
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Basic " + encodedAuth)
                .get()
                .build();

        try (Response response = httpClient.execute(request)) {
            if (response.isSuccessful()) {
                monitor.debug(format("HTTP request to %s was successful with status code %d and message %s",
                        url, response.code(), response.message()));

                byte[] responseBody = response.body().bytes();
                String responseHeaders = response.headers().toString();

                byte[] messageBytes = (responseHeaders + "\n").getBytes("ASCII");
                byte[] combined = new byte[messageBytes.length + responseBody.length];

                System.arraycopy(messageBytes, 0, combined, 0, messageBytes.length);
                System.arraycopy(responseBody, 0, combined, messageBytes.length, responseBody.length);

                try {
                    MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()), new ByteArrayInputStream(combined));
                    MimeMultipart multipart = (MimeMultipart) message.getContent();

                    List<byte[]> dicomDataList = new ArrayList<>();
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart part = multipart.getBodyPart(i);
                        byte[] dicomData = part.getInputStream().readAllBytes();
                        dicomDataList.add(dicomData);
                    }
                    return Result.success(dicomDataList);
                } catch (MessagingException e) {
                    return Result.failure("Failed to parse MIME message: " + e.getMessage());
                }
            } else {
                return Result.failure("Failed to execute WADO-RS: " + response.message());
            }
        } catch (IOException e) {
            return Result.failure("Exception occurred during HTTP request: " + e.getMessage());
        }
    }
}