package ovroogs.sql.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({})
@Retention(RUNTIME)
public @interface ForeignKey {
    String internalColumn();
    String externalColumn();
    Class<?> targetEntity();
    ActionType update() default ActionType.NO_ACTION;
    ActionType delete() default ActionType.NO_ACTION;
}