package org.xutils.download;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "book_download", onCreated = "CREATE UNIQUE INDEX index_name ON book_download(url,fileSavePath)")
public class DownloadInfo implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DownloadInfo() {
    }

    @Column(name = "id", isId = true)
    private long id;

    public String getDePath() {
        return dePath;
    }
private String size;
    public String getSize() {
	return size;
}

public void setSize(String size) {
	this.size = size;
}

	public void setDePath(String dePath) {
        this.dePath = dePath;
    }
    @Column(name = "bookId")
    private int bookId;
    
    @Column(name="decompressionPath")
    private String dePath;

    @Column(name = "book_type")
    private int bookType;//0,同步教材；1,课文朗读
    
    @Column(name = "state")
    private DownloadState state = DownloadState.STOPPED;

    @Column(name = "url")
    private String url;//下载URL

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Column(name = "imgUrl")
    private String imgUrl;//封面URL

    @Column(name = "bookName")
    private String bookName;
    
    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    @Column(name = "fileSavePath")
    private String fileSavePath;//zip保存路径

    @Column(name = "progress")
    private int progress;//当前进度

    @Column(name = "fileLength")
    private long fileLength;

    @Column(name = "autoResume")
    private boolean autoResume;

    @Column(name = "autoRename")
    private boolean autoRename;
    
    @Column(name = "userStop")
    private boolean userStop;
    
//    @Column(name = "reading_type")
    private int readingType;

    public int getReadingType() {
        return readingType;
    }

    public void setReadingType(int readingType) {
        this.readingType = readingType;
    }

    public int getBookType() {
        return bookType;
    }

    public void setBookType(int bookType) {
        this.bookType = bookType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DownloadState getState() {
        return state;
    }

    public void setState(DownloadState state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public boolean isUserStop() {
		return userStop;
	}

	public void setUserStop(boolean userStop) {
		this.userStop = userStop;
	}

	public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public boolean isAutoResume() {
        return autoResume;
    }

    public void setAutoResume(boolean autoResume) {
        this.autoResume = autoResume;
    }

    public boolean isAutoRename() {
        return autoRename;
    }

    public void setAutoRename(boolean autoRename) {
        this.autoRename = autoRename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DownloadInfo)) return false;

        DownloadInfo that = (DownloadInfo) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "id=" + id +
                ", dePath='" + dePath + '\'' +
                ", state=" + state +
                ", url='" + url + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", bookName='" + bookName + '\'' +
                ", fileSavePath='" + fileSavePath + '\'' +
                ", progress=" + progress +
                ", fileLength=" + fileLength +
                ", autoResume=" + autoResume +
                ", autoRename=" + autoRename +
                ", bookType=" + bookType +
                '}';
    }
}
