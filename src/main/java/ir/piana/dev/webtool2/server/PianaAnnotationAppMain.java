package ir.piana.dev.webtool2.server;

import ir.piana.dev.webtool2.server.annotation.*;
import ir.piana.dev.webtool2.server.http.BaseHttpServer;
import ir.piana.dev.webtool2.server.route.RouteService;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.lang.reflect.Method;

/**
 * @author Mohammad Rahmati, 4/23/2017 12:13 PM
 */
public abstract class PianaAnnotationAppMain {
    protected static Logger logger = Logger
            .getLogger(PianaAnnotationAppMain.class);
    private static BaseHttpServer httpServer = null;

    public static void start(Class pianaServerAnnotatedClass)
            throws Exception {
        PianaServer pianaServer = AnnotationController
                .getPianaServer(pianaServerAnnotatedClass);
        httpServer = BaseHttpServer.createServer(pianaServer);
        httpServer.start();
        logger.debug("server started. please wait for initialize...");
    }
}