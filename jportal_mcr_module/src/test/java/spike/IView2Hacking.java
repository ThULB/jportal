package spike;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mycore.imagetiler.MCRImage;
import org.mycore.iview2.services.MCRIView2Tools;

public class IView2Hacking {
    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        System.setProperty("MCR.Module-iview2.DirectoryForTiles", "tiles");
    }

//    @Test
    public void getTileFile() throws Exception {
        System.out.println("Tile dir: " + MCRIView2Tools.getTileDir().getAbsolutePath());
        File tiledFile = MCRImage.getTiledFile(MCRIView2Tools.getTileDir(), "jportal_derivate_00000002", null);
        for (File file : tiledFile.listFiles()) {
            System.out.println("files: " + file.getAbsolutePath());
        }
    }
}
