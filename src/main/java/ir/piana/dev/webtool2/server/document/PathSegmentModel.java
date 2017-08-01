package ir.piana.dev.webtool2.server.document;

/**
 * Created by SYSTEM on 8/1/2017.
 */
public class PathSegmentModel {
    private String name;
    private PathSegmentType type;

    public PathSegmentModel() {
    }

    public PathSegmentModel(String name, PathSegmentType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PathSegmentType getType() {
        return type;
    }

    public void setType(PathSegmentType type) {
        this.type = type;
    }
}
