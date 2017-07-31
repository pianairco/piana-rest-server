package ir.piana.dev.webtool2.server.annotation;

import ir.piana.dev.webtool2.server.role.RoleType;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ASUS on 7/28/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) //on class level
public @interface MethodHandler {
    RoleType requiredRole() default RoleType.NEEDLESS;
    String httpMethod() default "GET";
    boolean sync() default true;
    boolean urlInjected() default false;
}
