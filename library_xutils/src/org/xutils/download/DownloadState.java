package org.xutils.download;

public enum DownloadState {
    WAITING(0), STARTED(1), FINISHED(2), DECOMPRESSIONING(3), DECOMPRESSIONED(4), STOPPED(5), ERROR(6);

    private final int value;

    DownloadState(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static DownloadState valueOf(int value) {
        switch (value) {
            case 0:
                return WAITING;
            case 1:
                return STARTED;
            case 2:
                return FINISHED;
            case 3:
                return DECOMPRESSIONING;
            case 4:
                return DECOMPRESSIONED;
            case 5:
                return STOPPED;
            case 6:
                return ERROR;
            default:
                return STOPPED;
        }
    }
}
