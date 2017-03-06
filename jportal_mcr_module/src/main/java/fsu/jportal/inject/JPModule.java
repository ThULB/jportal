package fsu.jportal.inject;

import com.google.inject.AbstractModule;

import fsu.jportal.mets.MetsAutoGenerator;
import fsu.jportal.mets.impl.MetsAutoGeneratorImpl;

public class JPModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MetsAutoGenerator.class).to(MetsAutoGeneratorImpl.class);
    }

}
