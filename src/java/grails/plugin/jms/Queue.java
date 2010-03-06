package grails.plugin.jms;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Queue
{
    String container() default "standard";
    String adapter() default "standard";
    String name() default "";
    String selector() default "";
}