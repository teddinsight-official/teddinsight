package ng.com.teddinsight.teddinsight_app.models;

public class ClientUpload {

    private String fileName;
    private String fileUrl;

    public ClientUpload() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
