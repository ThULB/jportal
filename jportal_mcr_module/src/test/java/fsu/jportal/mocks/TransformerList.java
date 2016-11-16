package fsu.jportal.mocks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by chi on 29.09.16.
 * @author Huu Chi Vu
 */
public class TransformerList<T, R> {
    List<Transformer<T, R>> parsers;

    public TransformerList() {
        this.parsers = new ArrayList<>();
    }

    public TransformerList<T, R> add(Transformer<T, R> transformer) {
        parsers.add(transformer);
        return this;
    }

    public Stream<R> transformToStream(T t) {
        return transform(t).stream();
    }

    public List<R> transform(T t) {
        return parsers.stream()
                      .filter(p -> p.test(t))
                      .map(p -> p.apply(t))
                      .collect(Collectors.toList());
    }
}
