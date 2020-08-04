package fsu.jportal.xml.stream;

import java.util.Optional;
import java.util.function.Function;

import javax.xml.stream.XMLStreamReader;

/**
 * Created by chi on 13.10.16.
 */
public interface Instruction extends Function<XMLStreamReader, Optional<String>> {}
