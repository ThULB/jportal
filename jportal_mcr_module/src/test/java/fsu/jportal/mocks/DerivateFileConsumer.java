package fsu.jportal.mocks;

import spike.mets.XMLWriter;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by chi on 16.11.16.
 * @author Huu Chi Vu
 */
public interface DerivateFileConsumer
        extends Function<BiFunction<String, String, Consumer<XMLWriter>>, Consumer<XMLWriter>> {
}
