package fsu.jportal.mocks;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by chi on 29.09.16.
 * @author Huu Chi Vu
 */
public interface Transformer<T, R> extends Predicate<T>, Function<T, R> {

}
