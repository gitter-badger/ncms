package com.softmotions.ncms.media;

import java.io.Serializable;
import java.util.List;

/**
 * Media entity: folder or file.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaEntity implements Serializable {
    /**
     * Primary key
     */
    Long id;

    /**
     * File of folder name
     */
    String name;

    /**
     * Parent folder path
     */
    String folder;

    /**
     * File content type.
     * Content type: 'internal/folder'
     * represents folder entry.
     */
    String contentType;

    /**
     * File length
     */
    Integer contentLength;

    /**
     * Brief file/folder decription.
     */
    String description;

    /**
     * List of assigned textual tags.
     */
    List<MediaEntityTag> tags;

    /**
     * Primary access list.
     */
    MediaEntityACL primaryACL;

    /**
     * Secondary access list.
     */
    MediaEntityACL secondaryACL;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MediaEntityTag> getTags() {
        return tags;
    }

    public void setTags(List<MediaEntityTag> tags) {
        this.tags = tags;
    }

    public MediaEntityACL getPrimaryACL() {
        return primaryACL;
    }

    public void setPrimaryACL(MediaEntityACL primaryACL) {
        this.primaryACL = primaryACL;
    }

    public MediaEntityACL getSecondaryACL() {
        return secondaryACL;
    }

    public void setSecondaryACL(MediaEntityACL secondaryACL) {
        this.secondaryACL = secondaryACL;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaEntity that = (MediaEntity) o;
        if (folder != null ? !folder.equals(that.folder) : that.folder != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return true;
    }

    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        return result;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("MediaEntity{");
        sb.append("id=").append(id);
        sb.append(", folder='").append(folder).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", contentType='").append(contentType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}