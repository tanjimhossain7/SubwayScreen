package ca.ucalgary.edu.ensf380;

/**
 * The Advertisement class represents an advertisement, including its metadata such as title, description, and file path.
 */
public class Advertisement {
    private int id;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private String filePath;

    /**
     * Constructs an Advertisement object with the specified parameters.
     * @param id the ID of the advertisement.
     * @param title the title of the advertisement.
     * @param description the description of the advertisement.
     * @param fileName the name of the file containing the advertisement.
     * @param fileType the type of the file (e.g., JPEG, PNG).
     * @param filePath the path to the file.
     */
    public Advertisement(int id, String title, String description, String fileName, String fileType, String filePath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.fileType = fileType;
        this.filePath = filePath;
    }

    /**
     * Gets the ID of the advertisement.
     * @return the ID of the advertisement.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the title of the advertisement.
     * @return the title of the advertisement.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description of the advertisement.
     * @return the description of the advertisement.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the file name of the advertisement.
     * @return the file name of the advertisement.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the file type of the advertisement.
     * @return the file type of the advertisement.
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Gets the file path of the advertisement.
     * @return the file path of the advertisement.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the ID of the advertisement.
     * @param id the new ID of the advertisement.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the title of the advertisement.
     * @param title the new title of the advertisement.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the description of the advertisement.
     * @param description the new description of the advertisement.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the file name of the advertisement.
     * @param fileName the new file name of the advertisement.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Sets the file type of the advertisement.
     * @param fileType the new file type of the advertisement.
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * Sets the file path of the advertisement.
     * @param filePath the new file path of the advertisement.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

