package ir.piana.dev.webtool2.server.document;

/**
 * Created by SYSTEM on 8/1/2017.
 */
public enum PathSegmentType {
    TEXT("1"),
    PARAM("2");

    private String code;

    PathSegmentType(String code) {
        this.code = code;
    }

    public String toString() {
        return code;
    }
}
