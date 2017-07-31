package ir.piana.dev.webtool2.server.annotation;

import org.reflections.Reflections;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by ASUS on 7/28/2017.
 */
public abstract class AnnotationController {
    public static Class getServerClass()
            throws Exception {
        Reflections reflections = new Reflections();
        Set<Class<?>> typesAnnotatedWith = reflections
                .getTypesAnnotatedWith(PianaServer.class);
        if(typesAnnotatedWith == null || typesAnnotatedWith.isEmpty())
            throw new Exception(
                    "any class not annotated with @PianaServer");
        else if(typesAnnotatedWith.size() == 1)
            return (Class) typesAnnotatedWith.toArray()[0];
        else
            throw new Exception(
                    "more than one class annotated with @PianaServer");
    }

    public static PianaServer getPianaServer(Class targetClass) {
        Annotation annotation = targetClass
                .getAnnotation(PianaServer.class);
        return annotation == null ? null : (PianaServer) annotation;
    }

    public static Parameter getParameterAnnotatedByMapParam(
            Method targetMethod)
            throws Exception {
        Parameter[] parameters = targetMethod.getParameters();
        Parameter targetParam = null;
        for (Parameter parameter : parameters) {
            if (parameter.getAnnotation(MapParam.class) != null)
                if (targetParam == null)
                    targetParam = parameter;
                else
                    throw new Exception("only one param can be exposed by BodyObjectParam.");
        }
        return targetParam;
    }

    public static Parameter getParameterAnnotatedByBodyObjectParam(
            Method targetMethod)
            throws Exception {
        Parameter[] parameters = targetMethod.getParameters();
        Parameter targetParam = null;
        for (Parameter parameter : parameters) {
            if (parameter.getAnnotation(BodyObjectParam.class) != null)
                if (targetParam == null) {
                    MethodHandler methodHandler = getMethodHandler(targetMethod);
                    if (!methodHandler.httpMethod().equalsIgnoreCase(HttpMethod.POST) &&
                            !methodHandler.httpMethod().equalsIgnoreCase(HttpMethod.PUT))
                        throw new Exception("this method not have put or post httpMethodType");
                    targetParam = parameter;
                } else
                    throw new Exception("only one param can be exposed by BodyObjectParam.");
        }
        return targetParam;
    }

    public static List<Class> getHandlerClasses() {
        List<Class> classes = new ArrayList<>();
        Reflections reflections = new Reflections();
        Set<Class<?>> typesAnnotatedWith = reflections
                .getTypesAnnotatedWith(Handler.class);
        if(typesAnnotatedWith != null && !typesAnnotatedWith.isEmpty())
            classes.addAll(typesAnnotatedWith);
        return classes;
    }

    public static Handler getHandler(Class targetClass) {
        Annotation annotation = targetClass
                .getAnnotation(Handler.class);
        return annotation == null ? null : (Handler) annotation;
    }

    public static AssetHandler getAssetHandler(Class targetClass) {
        Annotation annotation = targetClass
                .getAnnotation(AssetHandler.class);
        return annotation == null ? null : (AssetHandler) annotation;
    }

    public static List<Method> getHandlerMethods(Class targetClass) {
        List<Method> methods = new ArrayList<>();
        final List<Method> allMethods = new ArrayList<Method>(
                Arrays.asList(targetClass.getDeclaredMethods()));
        for (final Method method : allMethods) {
            if (method.isAnnotationPresent(MethodHandler.class)) {
                Annotation annotInstance = method.getAnnotation(MethodHandler.class);
                // TODO process annotInstance
                methods.add(method);
            }
        }
//        Reflections reflections = new Reflections();
//        Set<Method> methodsAnnotatedWith = reflections
//                .getMethodsAnnotatedWith(MethodHandler.class);
//        if(methodsAnnotatedWith != null && !methodsAnnotatedWith.isEmpty())
//            methods.addAll(methodsAnnotatedWith);
        return methods;
    }

    public static MethodHandler getMethodHandler(Method targetMethod){
        Annotation annotation = targetMethod
                .getAnnotation(MethodHandler.class);
        return annotation == null ? null : (MethodHandler) annotation;
    }

    public static void main(String[] args) {
        Reflections reflections = new Reflections();
        Set<Class<?>> typesAnnotatedWith = reflections
                .getTypesAnnotatedWith(Path.class);
        typesAnnotatedWith.forEach(
                a -> System.out.println(a.toString()));
    }
}