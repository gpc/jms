package grails.jms;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Queue
{
    String name() default "";
    String selector() default "";
    String messageConverter() default "";
}