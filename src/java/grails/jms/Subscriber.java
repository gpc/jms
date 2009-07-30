package grails.jms;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscriber
{
    String topic() default "";
    String selector() default "";
    boolean durable() default false;
}