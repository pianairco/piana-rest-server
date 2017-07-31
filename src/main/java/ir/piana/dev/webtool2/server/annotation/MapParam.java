package ir.piana.dev.webtool2.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by SYSTEM on 7/31/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER) //on param level
public @interface MapParam {
}
