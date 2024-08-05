package ca.ucalgary.edu.ensf380;

public class Advertisement {
    private int id;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private String filePath;

    public Advertisement(int id, String title, String description, String fileName, String fileType, String filePath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.fileType = fileType;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFilePath() {
        return filePath;
    }
}
