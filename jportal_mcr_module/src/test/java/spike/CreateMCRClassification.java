package spike;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;

public class CreateMCRClassification {

//    @Test
    public void test() {
        MCRCategory category = null;
        MCRCategoryDAOFactory.getInstance().addCategory(null, category);
        fail("Not yet implemented");
    }

}
