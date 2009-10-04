package grails.jms;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscriber
{
    String container() default "standard";
    String adapter() default "standard";
    String topic() default "";
    String selector() default "";
    boolean durable() default false;
    String messageConverter() default "";
}