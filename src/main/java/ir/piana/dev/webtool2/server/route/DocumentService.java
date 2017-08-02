package ir.piana.dev.webtool2.server.route;

import ir.piana.dev.webtool2.server.annotation.*;
import ir.piana.dev.webtool2.server.asset.PianaAsset;
import ir.piana.dev.webtool2.server.asset.PianaAssetResolver;
import ir.piana.dev.webtool2.server.document.*;
import ir.piana.dev.webtool2.server.response.PianaResponse;
import ir.piana.dev.webtool2.server.session.Session;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SYSTEM on 7/17/2017.
 */
@Handler(baseUrl = "piana-doc")
class DocumentService extends RouteService {
    private PianaAssetResolver assetResolver = PianaAssetResolver
            .getInstance(".");

    @PianaServerProvider
    public static PianaServer pianaServer;

    @MethodHandler
    public static PianaResponse getPianaDocument(
            Session session,
            @MapParam Map<String, List<String>> map) {
        PianaAsset asset = null;
        try {
            asset = DocumentResolver
                    .getPianaDocumentHtml(pianaServer);
        } catch (Exception e) {
            return notFoundResponse();
        }
        return new PianaResponse(
                Response.Status.OK, 0,
                asset.getBytes(),
                MediaType.TEXT_HTML);
    }

//    @MethodHandler
//    @Path("json-model")
//    public static PianaResponse getPianaJson(
//            Session session,
//            @MapParam Map<String, List<String>> map) {
//        List<ServiceModel> serviceModels = new ArrayList<>();
//        ServiceModel serviceModel = new ServiceModel("hello-world",
//                false, MediaType.APPLICATION_JSON,
//                new ArrayList<String>(Arrays.asList("family")),
//                new ArrayList<String>(Arrays.asList("name")), HttpMethod.GET);
//        PathModel pathModel = new PathModel();
//        pathModel.paths.add(new PathSegmentModel("hello-world", PathSegmentType.TEXT));
//        pathModel.paths.add(new PathSegmentModel("family", PathSegmentType.PARAM));
//        serviceModel.setPathModel(pathModel);
//        serviceModels.add(serviceModel);
//        return new PianaResponse(
//                Response.Status.OK, 0,
//                serviceModels,
//                MediaType.APPLICATION_JSON);
//    }

    private static List<ServiceModel> serviceModels = null;

    @MethodHandler
    @Path("service-models")
    public static PianaResponse getPianaServiceModels(
            Session session,
            @MapParam Map<String, List<String>> map) {

        if(serviceModels != null)
            return new PianaResponse(
                    Response.Status.OK, 1,
                    serviceModels,
                    MediaType.APPLICATION_JSON);

        serviceModels = new ArrayList<>();
        List<Class> handlerClasses = AnnotationController
                .getHandlerClasses();
        handlerClasses.remove(DocumentService.class);
        for (Class targetClass : handlerClasses) {
            Handler handler = AnnotationController.getHandler(targetClass);
            if(handler.handlerType() == HandlerType.ASSET_HANDLER) {
                ServiceModel serviceModel = new ServiceModel();
                serviceModel.setAsset(true);
                serviceModel.setMethodType("GET");
                serviceModel.setReturnType("TEXT/ASSET/FILE");
                serviceModel.setPathModel(createAssetPathModel(targetClass));
                serviceModels.add(serviceModel);
            } else if(handler.handlerType() == HandlerType.METHOD_HANDLER) {
                List<Method> targetMethods = AnnotationController.
                        getHandlerMethods(targetClass);
                for(Method targetMethod : targetMethods) {
                    ServiceModel serviceModel = new ServiceModel();
                    serviceModel.setAsset(false);
                    serviceModel.setReturnType("APPLICATION/JSON");
                    MethodHandler methodHandler = AnnotationController
                            .getMethodHandler(targetMethod);
                    serviceModel.setMethodType(methodHandler.httpMethod());
                    List<Parameter> queryParams = AnnotationController
                            .getParameterAnnotatedByQueryParam(targetMethod);
                    List<String> queryParamsNames = new ArrayList<>();
                    for(Parameter param : queryParams) {
                        queryParamsNames.add(param.getAnnotation(QueryParam.class).value());
                    }

                    serviceModel.setQueryParams(queryParamsNames);
                    serviceModel.setPathModel(createPathModel(targetClass, targetMethod));
                    serviceModels.add(serviceModel);
                }
            }

        }


//        ServiceModel serviceModel = new ServiceModel(
//                false, MediaType.APPLICATION_JSON,
//                new ArrayList<String>(Arrays.asList("name")), HttpMethod.GET);
//        PathModel pathModel = new PathModel();
//        pathModel.paths.add(new PathSegmentModel("hello-world", PathSegmentType.TEXT));
//        pathModel.paths.add(new PathSegmentModel("family", PathSegmentType.PARAM));
//        serviceModel.setPathModel(pathModel);
//        serviceModels.add(serviceModel);

        return new PianaResponse(
                Response.Status.OK, 1,
                serviceModels,
                MediaType.APPLICATION_JSON);
    }

    protected static PianaResponse notFoundResponse() {
        return new PianaResponse(
                Response.Status.NOT_FOUND, 0,
                "not found asset",
                MediaType.TEXT_PLAIN);
    }

    protected static PathModel createAssetPathModel(Class targetClass) {
        PathModel pathModel = new PathModel();
        String url = "http://".concat(pianaServer.httpIp()).concat(":")
                .concat(String.valueOf(pianaServer.httpPort())).concat("/")
                .concat(pianaServer.httpBaseUrl())
                .concat(AnnotationController.getHandler(targetClass).baseUrl());
        String[] split = url.split("//");
        for(String seg : split) {
            if(seg.startsWith("{") && seg.endsWith("}"))
                pathModel.paths.add(new PathSegmentModel(
                        seg.substring(1).substring(0, seg.length() - 2),
                        PathSegmentType.PARAM));
            else if(!seg.startsWith("{") && !seg.endsWith("}"))
                pathModel.paths.add(new PathSegmentModel(seg, PathSegmentType.TEXT));
        }
        return pathModel;
    }

    protected static PathModel createPathModel(Class targetClass, Method method) {
        PathModel pathModel = new PathModel();
        String url = pianaServer.httpBaseUrl() == null || pianaServer.httpBaseUrl().isEmpty() ?
                pianaServer.httpBaseUrl() : pianaServer.httpBaseUrl().concat("/");
        String baseUrl = AnnotationController.getHandler(targetClass).baseUrl();
        if(baseUrl == null || baseUrl.isEmpty())
            url = url.concat("");
        else
            url = url.concat(baseUrl).concat("/");
        url = url.concat(AnnotationController.getPathAnnotation(method).value());
        url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        url = url.startsWith("/") ? url.substring(1) : url;
        String[] split = url.split("/");
        for(String seg : split) {
            if(seg.startsWith("{") && seg.endsWith("}"))
                pathModel.paths.add(new PathSegmentModel(
                        seg.substring(1).substring(0, seg.length() - 2),
                        PathSegmentType.PARAM));
            else if(!seg.startsWith("{") && !seg.endsWith("}"))
                pathModel.paths.add(new PathSegmentModel(seg, PathSegmentType.TEXT));
        }
        return pathModel;
    }
}
