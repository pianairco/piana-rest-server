package ir.piana.dev.webtool2.server.document;

import java.util.List;

/**
 * Created by SYSTEM on 7/19/2017.
 */
public class ServiceModel {
//    private String resourceBaseUrl;
    private Boolean asset;
    private String returnType;
//    private List<String> pathParams;
    private List<String> queryParams;
    private String methodType;
    private PathModel pathModel;

    public PathModel getPathModel() {
        return pathModel;
    }

    public void setPathModel(PathModel pathModel) {
        this.pathModel = pathModel;
    }

    public ServiceModel() {
    }

    public ServiceModel(
            Boolean asset, String returnType,
            List<String> queryParams,
            String methodType) {
        this.asset = asset;
        this.returnType = returnType;
        this.queryParams = queryParams;
        this.methodType = methodType;
    }

//    public ServiceModel(String resourceBaseUrl, Boolean asset, String returnType, List<String> pathParams, List<String> queryParams, String methodType) {
//        this.resourceBaseUrl = resourceBaseUrl;
//        this.asset = asset;
//        this.returnType = returnType;
//        this.pathParams = pathParams;
//        this.queryParams = queryParams;
//        this.methodType = methodType;
//    }

//    public String getResourceBaseUrl() {
//        return resourceBaseUrl;
//    }
//
//    public void setResourceBaseUrl(String resourceBaseUrl) {
//        this.resourceBaseUrl = resourceBaseUrl;
//    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

//    public List<String> getPathParams() {
//        return pathParams;
//    }

//    public void setPathParams(List<String> pathParams) {
//        this.pathParams = pathParams;
//    }

    public List<String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<String> queryParams) {
        this.queryParams = queryParams;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public Boolean getAsset() {
        return asset;
    }

    public void setAsset(Boolean asset) {
        this.asset = asset;
    }
}
