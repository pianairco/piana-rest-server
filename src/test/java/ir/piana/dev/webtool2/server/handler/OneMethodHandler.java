package ir.piana.dev.webtool2.server.handler;

import ir.piana.dev.webtool2.server.annotation.MapParam;
import ir.piana.dev.webtool2.server.response.PianaResponse;
import ir.piana.dev.webtool2.server.annotation.Handler;
import ir.piana.dev.webtool2.server.annotation.HandlerType;
import ir.piana.dev.webtool2.server.annotation.MethodHandler;
import ir.piana.dev.webtool2.server.role.RoleType;
import ir.piana.dev.webtool2.server.session.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Created by SYSTEM on 7/30/2017.
 */
@Handler(baseUrl = "hello-world", handlerType = HandlerType.METHOD_HANDLER)
public class OneMethodHandler {

    @MethodHandler(requiredRole = RoleType.GUEST, sync = false)
    public static PianaResponse getHello(
            Session session,
            @MapParam Map<String, List<String>> map,
            @QueryParam("name") String name) {
        return new PianaResponse(Response.Status.OK, 1, name);
    }
}