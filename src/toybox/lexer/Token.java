package toybox.lexer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates that a given method produces token objects
 * for the specified regular expression(s).
 * 
 * @author Bruno Dufour (dufour@iro.umontreal.ca)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Token {
	String[] value();
}
