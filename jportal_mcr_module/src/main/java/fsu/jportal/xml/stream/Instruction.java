package fsu.jportal.xml.stream;

import javax.xml.stream.XMLStreamReader;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by chi on 13.10.16.
 */
public interface Instruction extends Function<XMLStreamReader, Optional<String>> {}
