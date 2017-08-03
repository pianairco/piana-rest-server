package ir.piana.dev.webtool2.server.route;

import ir.piana.dev.secure.random.SecureRandomMaker;
import ir.piana.dev.secure.random.SecureRandomType;
import ir.piana.dev.secure.util.HexConverter;
import ir.piana.dev.webtool2.server.annotation.*;
import ir.piana.dev.webtool2.server.role.RoleType;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Created by SYSTEM on 7/18/2017.
 */
public abstract class HandlerMethodCreator {
    final static Logger logger =
            Logger.getLogger(HandlerMethodCreator.class);
    protected String methodKey;
    protected ForbiddenCheckable forbiddenCheckable;

    private HandlerMethodCreator(ForbiddenCheckable forbiddenCheckable) {
        this.forbiddenCheckable = forbiddenCheckable;
    }

    protected abstract String makeMethod() throws Exception;
    protected abstract String makeMethodAnnotation() throws Exception;
    protected abstract String makeMethodSignature() throws Exception;
    protected abstract String makeForbiddenChecker();
    protected abstract String makeRestOfBody() throws Exception;

    protected String makeMethodEnd() {
        return "}\n";
    }

    protected String makeMethodBody() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(makeMethodAnnotation());
        sb.append(makeMethodSignature());
        sb.append(makeForbiddenChecker());
        sb.append(makeRestOfBody());
        sb.append(makeMethodEnd());
        return sb.toString();
    }

    public String create()
            throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(makeMethod());
        return sb.toString();
    }

    // ---------------------- utility classes ---------------------------

    protected static String checkForbiddenForSync(RoleType routeRoleType) {
        StringBuilder sb = new StringBuilder();
        if (routeRoleType != RoleType.NEEDLESS) {
            sb.append("final Session session = doAuthorization(httpHeaders);\n");
            sb.append("if(session != null && session.isWrongdoer())\n"
                    .concat("return createResponse(forbiddenPianaResponse, session, httpHeaders);\n"));
        } else {
            sb.append("final Session session = doRevivalSession(httpHeaders);\n");
            sb.append("if(session != null && session.isWrongdoer())\n"
                    .concat("return createResponse(forbiddenPianaResponse, session, httpHeaders);\n"));
        }
        return sb.toString();
    }

    protected static String checkForbiddenForAsync(RoleType routeRoleType) {
        StringBuilder sb = new StringBuilder();
        if (routeRoleType != RoleType.NEEDLESS) {
            sb.append("final Session session = doAuthorization(httpHeaders);\n");
            sb.append("if(session != null && session.isWrongdoer()) {\n"
                    .concat("asyncResponse.resume(createResponse(forbiddenPianaResponse, session, httpHeaders));\n")
                    .concat("return;\n")
                    .concat("}\n"));
        } else {
            sb.append("final Session session = doRevivalSession(httpHeaders);\n");
            sb.append("if(session != null && session.isWrongdoer()) {\n"
                    .concat("asyncResponse.resume(createResponse(forbiddenPianaResponse, session, httpHeaders));\n")
                    .concat("return;\n")
                    .concat("}\n"));
        }
        return sb.toString();
    }

    // -------------------------- factory --------------------------------

    private interface ForbiddenCheckable {
        String checkForbidden(RoleType roleType);
    }

    public static HandlerMethodCreator getAssetHandlerInstance(
            Class targetClass)
            throws Exception {
        AssetHandler assetHandler = AnnotationController
                .getAssetHandler(targetClass);
        HandlerMethodCreator methodCreator = null;
        boolean isSync = assetHandler.sync();
        String methodKey = "GET#"
                .concat(AnnotationController.getHandler(targetClass).baseUrl())
                .concat("File");
        if(isSync)
            methodCreator = new SyncAssetHandlerMethodCreator(
                    assetHandler, HandlerMethodCreator::checkForbiddenForSync);
        else
            methodCreator = new AsyncAssetHandlerMethodCreator(
                    assetHandler, HandlerMethodCreator::checkForbiddenForAsync);
        return methodCreator;
    }

    public static HandlerMethodCreator getMethodHandlerInstance(
            Method targetMethod)
            throws Exception {
        MethodHandler methodHandler = AnnotationController
                .getMethodHandler(targetMethod);
        boolean isSync = methodHandler.sync();

        HandlerMethodCreator methodCreator = null;
        if(isSync)
            methodCreator = new SyncMethodHandlerMethodCreator(
                    targetMethod, methodHandler, HandlerMethodCreator::checkForbiddenForSync);
        else
            methodCreator = new ASyncMethodHandlerMethodCreator(
                    targetMethod, methodHandler, HandlerMethodCreator::checkForbiddenForAsync);

        return methodCreator;
    }

    // -------------------------- sub classes ----------------------------

    private abstract static class  AssetHandlerMethodCreator
            extends HandlerMethodCreator {
        protected AssetHandler assetHandler;

        private AssetHandlerMethodCreator (
                AssetHandler assetHandler,
                ForbiddenCheckable forbiddenCheckable) {
            super(forbiddenCheckable);
            this.assetHandler = assetHandler;
        }

        public String makeMethod()
                throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append(makeMethodBody());

            sb.append(makeMethodAnnotationWithoutPath());
            sb.append(makeMethodSignatureWithoutPath());
            sb.append(makeForbiddenChecker());
            sb.append(makeRestOfBody());
            sb.append(makeMethodEnd());
            return sb.toString();
        }

        @Override
        protected String makeMethodAnnotation() {
            StringBuilder sb = new StringBuilder();
            sb.append("@GET\n");
            sb.append("@Path(\"/{file:.*}\")\n");
            return sb.toString();
        }

        protected abstract String makeMethodSignatureWithoutPath();

        protected String makeMethodAnnotationWithoutPath() {
            StringBuilder sb = new StringBuilder("@GET\n");
            return sb.toString();
        }

        @Override
        protected String makeForbiddenChecker() {
            StringBuilder sb = new StringBuilder();
            sb.append(forbiddenCheckable.checkForbidden(assetHandler.requiredRole()));
            return sb.toString();
        }
    }

    private static class SyncAssetHandlerMethodCreator
            extends AssetHandlerMethodCreator {
        private SyncAssetHandlerMethodCreator(
                AssetHandler assetHandler,
                ForbiddenCheckable forbiddenCheckable) {
            super(assetHandler, forbiddenCheckable);
        }

        @Override
        protected String makeMethodSignature() {
            StringBuilder sb = new StringBuilder();
            sb.append("public Response getFile(@Context HttpHeaders httpHeaders,"
                    .concat("@Context UriInfo uriInfo,")
                    .concat("@PathParam(\"file\") String file)")
                    .concat("throws Exception {\n"));
            return sb.toString();
        }

        @Override
        protected String makeMethodSignatureWithoutPath(){
            StringBuilder sb = new StringBuilder();
            sb.append("public Response getFileWithoutParam(@Context HttpHeaders httpHeaders,"
                    .concat("@Context UriInfo uriInfo)")
                    .concat("throws Exception {\nString file = null;\n"));
            return sb.toString();
        }

        @Override
        protected String makeRestOfBody()
                throws Exception {
            StringBuilder sb = new StringBuilder();
            if (assetHandler.requiredRole() != RoleType.NEEDLESS) {
                sb.append("if(!RoleType."
                        .concat(assetHandler.requiredRole().getName())
                        .concat(".isValid(session.getRoleType()))\n")
                        .concat("return createResponse(unauthorizedPianaResponse, session, httpHeaders);\n"));
            }
            sb.append("if(file == null || file.isEmpty()) {\n");
            sb.append("if(!isAssetExist(\""
                    .concat(assetHandler.assetPath()).concat("\", \"index.html\"))\n")
                    .concat("return createResponse(notFoundPianaResponse, session, httpHeaders);\n")
            );
            sb.append("} else {\n");
            sb.append("if(!isAssetExist(\""
                    .concat(assetHandler.assetPath())
                    .concat("\",file))\n")
                    .concat("return createResponse(notFoundPianaResponse, session, httpHeaders);\n")
                    .concat("}\n")
            );
            sb.append("try {\n");


            ///start of register method
            String registerMethod = "Method m = registerMethod(\""
                    .concat("getFile")
                    .concat("\",");

            registerMethod = registerMethod.concat(
                    "\"ir.piana.dev.webtool2.server.route.AssetService\",")
                    .concat("\"getAsset\",");
            registerMethod = registerMethod.concat("Session.class,");
            registerMethod = registerMethod
                    .concat("PianaAssetResolver.class,");
            registerMethod = registerMethod.concat(
                    "String.class");
            sb.append(registerMethod);
            sb.append(");\n");
            sb.append("return createResponse(invokeMethod(m, session, assetResolver, file),");
//            if(assetHandler.urlInjected())
//                sb.append("createMapParam(uriInfo,\""
//                        .concat(urlPattern)
//                        .concat("\",\"")
//                        .concat(methodPattern)
//                        .concat("\"),"));
//            else
//                sb.append("createMapParam(uriInfo),");
            sb.append(" session, httpHeaders);\n");

            ///end of invokeMethod
            String excName = "exc_".concat(HexConverter.toHexString(
                    SecureRandomMaker.makeByteArray(
                            16, SecureRandomType.SHA_1_PRNG)));
            sb.append("} catch (Exception ".concat(excName).concat(") {\n"));
            sb.append("System.out.println(".concat(excName).concat(".getMessage());\n"));
            sb.append("logger.error(".concat(excName).concat(");\n"));
            sb.append("return createResponse(internalServerErrorPianaResponse, session, httpHeaders);\n}\n");
            return sb.toString();
        }
    }

    private static class AsyncAssetHandlerMethodCreator
            extends AssetHandlerMethodCreator {
        private AsyncAssetHandlerMethodCreator(
                AssetHandler assetHandler,
                ForbiddenCheckable forbiddenCheckable) {
            super(assetHandler, forbiddenCheckable);
        }

        @Override
        protected String makeMethodSignatureWithoutPath() {
            StringBuilder sb = new StringBuilder();
            sb.append("public void getFileWithoutParam"
                    .concat("(@Suspended final AsyncResponse asyncResponse,")
                    .concat("@Context HttpHeaders httpHeaders,")
                    .concat("@Context UriInfo uriInfo")
                    .concat(") throws Exception {\nString file = null;\n"));
            return sb.toString();
        }

        @Override
        protected String makeMethodSignature() {
            StringBuilder signature = new StringBuilder();
            signature.append("public void getFile"
                    .concat("(@Suspended final AsyncResponse asyncResponse,")
                    .concat("@Context HttpHeaders httpHeaders,")
                    .concat("@Context UriInfo uriInfo,")
                    .concat("@PathParam(\"file\") String file")
                    .concat(") throws Exception {\n"));
            return signature.toString();
        }

        @Override
        protected String makeRestOfBody() throws Exception {
            StringBuilder sb = new StringBuilder();
            if (assetHandler.requiredRole() != RoleType.NEEDLESS) {
                sb.append("if(!RoleType."
                        .concat(assetHandler.requiredRole().getName())
                        .concat(".isValid(session.getRoleType())){\n")
                        .concat("asyncResponse.resume(createResponse(unauthorizedPianaResponse, session, httpHeaders));\n")
                        .concat("return;")
                        .concat("}\n"));
            }
            sb.append("if(file == null || file.isEmpty()) {\n");
            sb.append("if(!isAssetExist(\""
                    .concat(assetHandler.assetPath()).concat("\", \"index.html\")){\n")
                    .concat("asyncResponse.resume(createResponse(notFoundPianaResponse, session, httpHeaders));\n")
                    .concat("return;\n")
                    .concat("}\n")
            );
            sb.append("} else {\n");
            sb.append("if(!isAssetExist(\""
                    .concat(assetHandler.assetPath())
                    .concat("\",file)){\n")
                    .concat("asyncResponse.resume(createResponse(notFoundPianaResponse, session, httpHeaders));\n")
                    .concat("return;\n")
                    .concat("}\n")
            );
            sb.append("}\n");

            sb.append("try {\n");
            ///start of register method
            String registerMethod = "Method m = registerMethod(\""
                    .concat("getFile")
                    .concat("\",");

            registerMethod = registerMethod.concat(
                    "\"ir.piana.dev.webtool2.server.route.AssetService\",")
                    .concat("\"getAsset\",");
            registerMethod = registerMethod.concat("Session.class,");
            registerMethod = registerMethod
                    .concat("PianaAssetResolver.class,");
            registerMethod = registerMethod.concat(
                    "String.class");
            sb.append(registerMethod);
            sb.append(");\n");
            sb.append("executorService.execute(() -> {\n");
            sb.append("try {\n");
            sb.append("asyncResponse.resume(createResponse(invokeMethod(m, session, assetResolver, file),");
            sb.append(" session, httpHeaders));\n");
            sb.append("} catch (Exception exc_cde17fe59f25cadd342c69673cba897d) {\n");
            sb.append("logger.error(exc_cde17fe59f25cadd342c69673cba897d);\n");
            sb.append("asyncResponse.resume(createResponse(internalServerErrorPianaResponse, session, httpHeaders));\n");
            sb.append("}\n");
            sb.append("});\n");
//            if(assetHandler.urlInjected())
//                sb.append("createMapParam(uriInfo,\""
//                        .concat(urlPattern)
//                        .concat("\",\"")
//                        .concat(methodPattern)
//                        .concat("\"),"));
//            else
//                sb.append("createMapParam(uriInfo),");

            ///end of invokeMethod
            String excName = "exc_".concat(HexConverter.toHexString(
                    SecureRandomMaker.makeByteArray(
                            16, SecureRandomType.SHA_1_PRNG)));
            sb.append("} catch (Exception ".concat(excName).concat(") {\n"));
            sb.append("logger.error(".concat(excName).concat(");\n"));
            sb.append("asyncResponse.resume(createResponse(internalServerErrorPianaResponse, session, httpHeaders));\n}\n");
            return sb.toString();
        }
    }

    //----------------------------------------------------------------------

    private static abstract class  MethodHandlerMethodCreator
            extends HandlerMethodCreator {
        protected MethodHandler methodHandler;
        protected Method method;
        private MethodHandlerMethodCreator (
                Method method, MethodHandler methodHandler,
                ForbiddenCheckable forbiddenCheckable) {
            super(forbiddenCheckable);
            this.method = method;
            this.methodHandler = methodHandler;
        }

        public String makeMethod()
                throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append(makeMethodBody());
            return sb.toString();
        }

        @Override
        protected String makeMethodAnnotation()
                throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("@".concat(methodHandler
                    .httpMethod()).concat("\n"));
            Path annotation = method.getAnnotation(Path.class);
            sb.append("@Path(\"".concat(annotation == null?
                    "" : annotation.value()).concat("\")\n"));
            Parameter bodyObjectParam = AnnotationController
                    .getParameterAnnotatedByBodyObjectParam(method);
            if(bodyObjectParam != null)
                sb.append("@Consumes(MediaType.APPLICATION_JSON)\n");
            return sb.toString();
        }

        @Override
        protected String makeForbiddenChecker() {
            StringBuilder sb = new StringBuilder();
            sb.append(forbiddenCheckable.checkForbidden(methodHandler.requiredRole()));
            return sb.toString();
        }
    }

    private static class SyncMethodHandlerMethodCreator
            extends MethodHandlerMethodCreator {
        private SyncMethodHandlerMethodCreator(
                Method method, MethodHandler methodHandler,
                ForbiddenCheckable forbiddenCheckable) {
            super(method, methodHandler, forbiddenCheckable);
        }

        @Override
        protected String makeMethodSignature()
                throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("public Response ".concat(method.getName())
                    .concat("(@Context HttpHeaders httpHeaders,"));
            Parameter[] parameters = method.getParameters();
            for (int i = 1; i < parameters.length; i++) {
                PathParam pathParam = parameters[i].getAnnotation(PathParam.class);
                QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
                BodyObjectParam bodyObjectParam = parameters[i]
                        .getAnnotation(BodyObjectParam.class);
                MapParam mapParam = parameters[i].getAnnotation(MapParam.class);
                if(pathParam != null)
                    sb.append("@PathParam(\"".concat(pathParam.value())
                            .concat("\")").concat(parameters[i].toString()).concat(","));
                else if(queryParam != null)
                    sb.append("@QueryParam(\"".concat(queryParam.value())
                            .concat("\")").concat(parameters[i].toString()).concat(","));
                else if(bodyObjectParam != null)
                    sb.append(parameters[i].toString().concat(","));
                else if(mapParam != null)
                    sb.append("@Context UriInfo uriInfo,");
                else
                    throw new Exception("this parameter should not be allowed present in this location.");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(") throws Exception {\n");
            return sb.toString();
        }

        @Override
        protected String makeRestOfBody()
                throws Exception {
            StringBuilder sb = new StringBuilder();
            if(methodHandler.requiredRole() != RoleType.NEEDLESS) {
                sb.append("if(session != null && !RoleType."
                        .concat(methodHandler.requiredRole().getName())
                        .concat(".isValid(session.getRoleType()))\n"));
                sb.append("return createResponse(unauthorizedPianaResponse, session, httpHeaders);\n");
            }
            sb.append("try {\n");
            sb.append("Method m = registerMethod(\""
                    .concat(method.getName())
                    .concat("\",\"").concat(method.getDeclaringClass().getName()).concat("\",\"")
                    .concat(method.getName()).concat("\",Session.class,"));
            Parameter[] parameters = method.getParameters();
            for (int i = 1; i < parameters.length; i++) {
                PathParam pathParam = parameters[i].getAnnotation(PathParam.class);
                QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
                BodyObjectParam bodyObjectParam = parameters[i]
                        .getAnnotation(BodyObjectParam.class);
                MapParam mapParam = parameters[i].getAnnotation(MapParam.class);
                if(pathParam != null || queryParam != null || bodyObjectParam != null)
                    sb.append(parameters[i].getType().getName().concat(".class,"));
                else if(mapParam != null)
                    sb.append("Map.class,");
                else
                    throw new Exception("parameter not allowed in this context");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(");\n");
            sb.append("return createResponse(invokeMethod(m, session,");
            for (int i = 1; i < parameters.length; i++) {
                PathParam pathParam = parameters[i].getAnnotation(PathParam.class);
                QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
                BodyObjectParam bodyObjectParam = parameters[i]
                        .getAnnotation(BodyObjectParam.class);
                MapParam mapParam = parameters[i].getAnnotation(MapParam.class);
                if(pathParam != null || queryParam != null || bodyObjectParam != null)
                    sb.append(parameters[i].getName().concat(","));
                else if (mapParam != null)
                    sb.append("createMapParam(uriInfo),");
                else
                    throw new Exception("parameter not allowed in this context");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("), session, httpHeaders);\n");
            sb.append("} catch (Exception exc_9b6bd4a8b95b5e820f471442ab506d75) {\n");
            sb.append("logger.error(this.getClass().getName() + \" : \" + exc_9b6bd4a8b95b5e820f471442ab506d75);\n");
            sb.append("return createResponse(internalServerErrorPianaResponse, session, httpHeaders);\n");
            sb.append("}\n");
            return sb.toString();
        }
    }

    private static class ASyncMethodHandlerMethodCreator
            extends MethodHandlerMethodCreator {
        private ASyncMethodHandlerMethodCreator(
                Method method, MethodHandler methodHandler,
                ForbiddenCheckable forbiddenCheckable) {
            super(method, methodHandler, forbiddenCheckable);
        }

        @Override
        protected String makeMethodSignature()
                throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("public void ".concat(method.getName())
                    .concat("(@Suspended final AsyncResponse asyncResponse,")
                    .concat("@Context HttpHeaders httpHeaders,"));

            Parameter[] parameters = method.getParameters();
            for (int i = 1; i < parameters.length; i++) {
                PathParam pathParam = parameters[i].getAnnotation(PathParam.class);
                QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
                BodyObjectParam bodyObjectParam = parameters[i]
                        .getAnnotation(BodyObjectParam.class);
                MapParam mapParam = parameters[i].getAnnotation(MapParam.class);
                if(pathParam != null)
                    sb.append("@PathParam(\"".concat(pathParam.value())
                            .concat("\")").concat(parameters[i].toString()).concat(","));
                else if(queryParam != null)
                    sb.append("@QueryParam(\"".concat(queryParam.value())
                            .concat("\")").concat(parameters[i].toString()).concat(","));
                else if(bodyObjectParam != null)
                    sb.append(parameters[i].toString().concat(","));
                else if(mapParam != null)
                    sb.append("@Context UriInfo uriInfo,");
                else
                    throw new Exception("this parameter should not be allowed present in this location.");
            }

            sb.deleteCharAt(sb.length() - 1);
            sb.append(") throws Exception {\n");
            return sb.toString();
        }

        @Override
        protected String makeRestOfBody() throws Exception {
            StringBuilder sb = new StringBuilder();
            if(methodHandler.requiredRole() != RoleType.NEEDLESS) {
                sb.append("if(session != null && !RoleType."
                        .concat(methodHandler.requiredRole().getName())
                        .concat(".isValid(session.getRoleType())){\n"));
                sb.append("asyncResponse.resume(createResponse(unauthorizedPianaResponse, session, httpHeaders));\n");
                sb.append("return;\n}\n");
            }
            sb.append("try {\n");
            sb.append("Method m = registerMethod(\""
                    .concat(method.getName())
                    .concat("\",\"").concat(method.getDeclaringClass().getName()).concat("\",\"")
                    .concat(method.getName()).concat("\",Session.class,"));
            Parameter[] parameters = method.getParameters();
            for (int i = 1; i < parameters.length; i++) {
                PathParam pathParam = parameters[i].getAnnotation(PathParam.class);
                QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
                BodyObjectParam bodyObjectParam = parameters[i]
                        .getAnnotation(BodyObjectParam.class);
                MapParam mapParam = parameters[i].getAnnotation(MapParam.class);
                if(pathParam != null || queryParam != null || bodyObjectParam != null)
                    sb.append(parameters[i].getType().getName().concat(".class,"));
                else if(mapParam != null)
                    sb.append("Map.class,");
                else
                    throw new Exception("parameter not allowed in this context");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(");\n");
            sb.append("asyncResponse.resume(createResponse(invokeMethod(m, session,");
            for (int i = 1; i < parameters.length; i++) {
                PathParam pathParam = parameters[i].getAnnotation(PathParam.class);
                QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
                BodyObjectParam bodyObjectParam = parameters[i]
                        .getAnnotation(BodyObjectParam.class);
                MapParam mapParam = parameters[i].getAnnotation(MapParam.class);
                if(pathParam != null || queryParam != null || bodyObjectParam != null)
                    sb.append(parameters[i].getName().concat(","));
                else if (mapParam != null)
                    sb.append("createMapParam(uriInfo),");
                else
                    throw new Exception("parameter not allowed in this context");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("), session, httpHeaders));\n");
            sb.append("} catch (Exception exc_9b6bd4a8b95b5e820f471442ab506d75) {\n");
            sb.append("logger.error(this.getClass().getName() + \" : \" + exc_9b6bd4a8b95b5e820f471442ab506d75);\n");
            sb.append("asyncResponse.resume(createResponse(internalServerErrorPianaResponse, session, httpHeaders));\n");
            sb.append("}\n");
            return sb.toString();
        }
    }

    //----------------------------------------------------------------------
}

