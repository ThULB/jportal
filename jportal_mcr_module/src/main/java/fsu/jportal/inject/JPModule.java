package fsu.jportal.inject;

import com.google.inject.AbstractModule;

import fsu.jportal.backend.JPVolumeTypeDetector;
import fsu.jportal.backend.gnd.GNDAreaCodesService;
import fsu.jportal.backend.gnd.GNDLocationService;
import fsu.jportal.backend.gnd.impl.GNDAreaCodesRTFService;
import fsu.jportal.backend.gnd.impl.GNDSRULocationService;
import fsu.jportal.backend.impl.JPVolumeTypeDefaultDetector;
import fsu.jportal.mets.MetsAutoGenerator;
import fsu.jportal.mets.impl.MetsAutoGeneratorImpl;

public class JPModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MetsAutoGenerator.class).to(MetsAutoGeneratorImpl.class);
        bind(JPVolumeTypeDetector.class).to(JPVolumeTypeDefaultDetector.class);
        bind(GNDAreaCodesService.class).to(GNDAreaCodesRTFService.class);
        bind(GNDLocationService.class).to(GNDSRULocationService.class);
    }

}
