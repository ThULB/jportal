package fsu.jportal.urn;

import org.junit.Assert;
import org.junit.Test;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRJPATestCase;
import org.mycore.pi.backend.MCRPI;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.pi.urn.MCRUUIDURNGenerator;

import javax.persistence.EntityTransaction;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Created by chi on 10.04.17.
 *
 * @author Huu Chi Vu
 */
public class URNToolsTest extends MCRJPATestCase{
    @Test
    public void getURNForFile() throws Exception {
        String identifier = "urn12345";
        MCRPI mcrpi = new MCRPI(identifier, MCRDNBURN.TYPE, "mycoreID", "/file 20.jpg", "DNBURNGranular", null);
        MCREntityManagerProvider.getCurrentEntityManager()
                                .persist(mcrpi);

        EntityTransaction transaction = MCREntityManagerProvider.getCurrentEntityManager().getTransaction();

        if(!transaction.isActive()){
            transaction.begin();
        }

        transaction.commit();

        String urn = URNTools.getURNForFile("mycoreID", "/file%2020.jpg");
        System.out.println("URN: " + urn);
        Assert.assertEquals("Urn should be: ", identifier, urn);
    }

    @Test
    public void urlDecode() throws Exception {
        String url = "jportal_derivate_00203695/alt-_129513253_1995_29_%200005.tif";

        String decode = URLDecoder.decode(url, "UTF-8");
        System.out.println("decode: " + decode);

    }

    @Override
    protected Map<String, String> getTestProperties() {
        Map<String, String> testProperties = super.getTestProperties();
        testProperties.put("MCR.Metadata.Type.test", Boolean.TRUE.toString());
        testProperties.put("MCR.PI.Registration.DNBURNGranular.Generator", "UUID");
        testProperties.put("MCR.PI.Registration.DNBURNGranular.supportDfgViewerURN", Boolean.TRUE.toString());
        testProperties.put("MCR.PI.Generator.UUID", MCRUUIDURNGenerator.class.getName());
        testProperties.put("MCR.PI.Generator.UUID.Namespace", "frontend-");
        return testProperties;
    }
}