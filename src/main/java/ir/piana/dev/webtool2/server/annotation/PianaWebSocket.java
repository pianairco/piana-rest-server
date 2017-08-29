package ir.piana.dev.webtool2.server.annotation;

import ir.piana.dev.webtool2.server.http.HttpServerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ASUS on 7/28/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //on class level
public @interface PianaWebSocket {
    String socketIp() default "localhost";
    int socketPort() default 8008;
}
