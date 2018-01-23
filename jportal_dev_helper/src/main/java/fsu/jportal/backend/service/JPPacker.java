package fsu.jportal.backend.service;

import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRUsageException;
import org.mycore.services.packaging.MCRPacker;

import java.util.concurrent.ExecutionException;

/**
 * Created by chi on 08.01.18.
 *
 * @author Huu Chi Vu
 */
public class JPPacker extends MCRPacker{
    @Override
    public void checkSetup() throws MCRUsageException, MCRAccessException {

    }

    @Override
    public void pack() throws ExecutionException {
    }

    @Override
    public void rollback() {

    }
}
