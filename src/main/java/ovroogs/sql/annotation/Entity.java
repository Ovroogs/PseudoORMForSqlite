package ovroogs.sql.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface Entity {
    String name() default "";
    ForeignKey[] foreignKeys() default {};
    UniqueConstraint[] uniqueConstraints() default {};
}