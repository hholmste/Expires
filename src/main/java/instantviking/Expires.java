package instantviking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class or method annotated with @instantviking.Expires is considered
 * deprecated with the addition of a hard date after which
 * usage will fail compilation.
 *
 * TODO: support constructors
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Expires
{
    int day();

    int month(); // 1 is January

    int year();

    String usage(); // what is a developer supposed to do instead?
}