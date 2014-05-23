package com.softmotions.ncms.media;

import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaCreateEvent extends BasicEvent {

    final Long id;

    final String path;

    final boolean isFolder;

    public MediaCreateEvent(Object source, boolean isFolder, Number id, String path) {
        super(source);
        this.id = id != null ? id.longValue() : null;
        this.path = path;
        this.isFolder = isFolder;
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public boolean isFolder() {
        return isFolder;
    }
}
