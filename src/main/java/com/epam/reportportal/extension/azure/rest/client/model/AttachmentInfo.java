package com.epam.reportportal.extension.azure.rest.client.model;

public class AttachmentInfo {
    final String fileName;
    final String fileId;
    final String url;
    final String contentType;

    public AttachmentInfo(String fileName, String fileId, String url, String contentType) {
        this.fileName = fileName;
        this.fileId = fileId;
        this.url = url;
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }
}
