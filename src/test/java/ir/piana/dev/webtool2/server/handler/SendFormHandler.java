package ir.piana.dev.webtool2.server.handler;

import ir.piana.dev.webtool2.server.annotation.BodyObjectParam;
import ir.piana.dev.webtool2.server.annotation.Handler;
import ir.piana.dev.webtool2.server.annotation.HandlerType;
import ir.piana.dev.webtool2.server.annotation.MethodHandler;
import ir.piana.dev.webtool2.server.model.UserModel;
import ir.piana.dev.webtool2.server.response.PianaResponse;
import ir.piana.dev.webtool2.server.role.RoleType;
import ir.piana.dev.webtool2.server.session.Session;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Created by SYSTEM on 7/31/2017.
 */
@Handler(baseUrl = "send-form")
public class SendFormHandler {
    @MethodHandler(requiredRole = RoleType.GUEST)
    @Path("")
    public static PianaResponse getHello(
            Session session,
            @QueryParam("input-fname") String fname,
            @QueryParam("input-tel") String tel) {
        return new PianaResponse(Response.Status.OK, 1, fname);
    }

    @MethodHandler(requiredRole = RoleType.GUEST, httpMethod = HttpMethod.POST)
    public static PianaResponse postHello(
            Session session,
            @BodyObjectParam UserModel userModel) {
        if(userModel == null)
            return new PianaResponse(Response.Status.OK, 0, "is null");
        return new PianaResponse(Response.Status.OK, 1, userModel.getFname());
    }
}
