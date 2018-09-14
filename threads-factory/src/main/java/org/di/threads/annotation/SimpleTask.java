package org.di.threads.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Annotation for configuring scheduling tasks in application context.
 *
 * @author GenCloud
 * @date 14.09.2018
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleTask {
    long startingDelay() default -1;

    long fixedInterval() default -1;

    TimeUnit unit() default TimeUnit.MILLISECONDS;
}
