package ir.piana.dev.webtool2.server.route;

import ir.piana.dev.webtool2.server.annotation.*;
import ir.piana.dev.webtool2.server.asset.PianaAsset;
import ir.piana.dev.webtool2.server.asset.PianaAssetResolver;
import ir.piana.dev.webtool2.server.document.*;
import ir.piana.dev.webtool2.server.response.PianaResponse;
import ir.piana.dev.webtool2.server.session.Session;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
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

    @MethodHandler
    @Path("json-model")
    public static PianaResponse getPianaJson(
            Session session,
            @MapParam Map<String, List<String>> map) {
        List<ServiceModel> serviceModels = new ArrayList<>();
        ServiceModel serviceModel = new ServiceModel("hello-world", "id1",
                false, MediaType.APPLICATION_JSON,
                new ArrayList<String>(Arrays.asList("family")),
                new ArrayList<String>(Arrays.asList("name")), HttpMethod.GET);
        PathModel pathModel = new PathModel();
        pathModel.paths.add(new PathSegmentModel("hello-world", PathSegmentType.TEXT));
        pathModel.paths.add(new PathSegmentModel("family", PathSegmentType.PARAM));
        serviceModel.setPathModel(pathModel);
        serviceModels.add(serviceModel);
        return new PianaResponse(
                Response.Status.OK, 0,
                serviceModels,
                MediaType.APPLICATION_JSON);
    }

    @MethodHandler
    @Path("service-models")
    public static PianaResponse getPianaServiceModels(
            Session session,
            @MapParam Map<String, List<String>> map) {
        List<ServiceModel> serviceModels = new ArrayList<>();
        ServiceModel serviceModel = new ServiceModel(pianaServer.httpBaseUrl(), "id1",
                false, MediaType.APPLICATION_JSON,
                new ArrayList<String>(Arrays.asList("family")),
                new ArrayList<String>(Arrays.asList("name")), HttpMethod.GET);
        PathModel pathModel = new PathModel();
        pathModel.paths.add(new PathSegmentModel("hello-world", PathSegmentType.TEXT));
        pathModel.paths.add(new PathSegmentModel("family", PathSegmentType.PARAM));
        serviceModel.setPathModel(pathModel);
        serviceModels.add(serviceModel);
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
}
