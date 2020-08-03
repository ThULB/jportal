package spike;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
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
        System.out.println("Tile dir: " + MCRIView2Tools.getTileDir().toAbsolutePath());
        Path tiledFile = MCRImage.getTiledFile(MCRIView2Tools.getTileDir(), "jportal_derivate_00000002", null);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tiledFile)) {
            for (Path path : directoryStream) {
                System.out.println("files: " + path.toAbsolutePath());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
