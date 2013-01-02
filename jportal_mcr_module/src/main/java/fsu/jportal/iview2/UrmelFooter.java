/*
 * $Id$
 * $Revision: 5697 $ $Date: 26.04.2010 $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */
package fsu.jportal.iview2;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.backend.hibernate.tables.MCRURN;
import org.mycore.iview2.frontend.MCRFooterInterface;

/**
 * @author Thomas Scheffler (yagee)
 *
 */
public class UrmelFooter implements MCRFooterInterface {
    static final Font MIN_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 9);

    static final Font MID_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    static final Font MAX_FONT = new Font(Font.MONOSPACED, Font.BOLD, 16);

    private static Logger LOGGER = Logger.getLogger(UrmelFooter.class);

    private final HashMap<String, BufferedImage> logos = new HashMap<String, BufferedImage>();

    private static final BufferedImage SAMPLE_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    public UrmelFooter() throws IOException {
        loadLogos();
    }

    private void loadLogos() throws IOException {
        //TODO get logos from classification
        HashMap<String, String> logoMap = new HashMap<String, String>();
        logoMap.put("dfg", "/web/images/dfg-logo.png");
        logoMap.put("thulb", "/web/images/thulb-blue.png");
        for (Map.Entry<String, String> entry : logoMap.entrySet()) {
            InputStream input = UrmelFooter.class.getResourceAsStream(entry.getValue());
            if(input == null) {
                LOGGER.error("Unable to load resource", new IOException(entry.getValue() + " not found"));
                continue;
            }
            logos.put(entry.getKey(), readImage(input));
        }
    }

    public BufferedImage getFooter(int imageWidth, String derivateID, String imagePath) {
        return getFooter(getFooterText(derivateID, imagePath), imageWidth, 50);
    }

    private String getFooterText(String derivateID, String imagePath) {
        String path, image;
        image = imagePath.substring(imagePath.lastIndexOf('/') + 1);
        path = imagePath.substring(0, imagePath.length() - image.length());
        LOGGER.info("path: " + path + ", image: " + image);
        Session session = MCRHIBConnection.instance().getSession();
        Criteria urnForImage = session.createCriteria(MCRURN.class);
        urnForImage.add(Restrictions.eq("key.mcrid", derivateID));
        urnForImage.add(Restrictions.eq("path", path));
        urnForImage.add(Restrictions.eq("filename", image));
        urnForImage.setProjection(Projections.property("key.mcrurn"));
        String urn = (String) urnForImage.uniqueResult();
        if (urn != null) {
            return urn;
        }
        return derivateID + ":" + imagePath;
    }

    private BufferedImage getFooter(String urn, final int width, int height) {
        Font font;
        if (width <= 256) {
            font = MIN_FONT;
        } else if (width <= 1024) {
            font = MID_FONT;
        } else {
            font = MAX_FONT;
        }
        FontRenderContext frc = SAMPLE_IMAGE.createGraphics().getFontRenderContext();
        LineMetrics lineMetric = font.getLineMetrics(urn, frc);
        List<String> printStr = getStringForWidth(urn, font, frc, width * 3 / 5);
        double lineHeight = 0, lineWidth = 0;
        for (String line : printStr) {
            Rectangle2D stringBounds = font.getStringBounds(line, frc);
            lineHeight = Math.max(stringBounds.getHeight(), lineHeight);
            lineWidth = Math.max(stringBounds.getWidth(), lineWidth);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(printStr + " dimensions: " + (int) lineWidth + "x" + (int) lineHeight);
        }
        int requiredHeight = (int) Math.ceil(lineHeight) * (printStr.size() + 1);
        if (requiredHeight > height) {
            height = requiredHeight;
            LOGGER.debug("min height for footer is " + requiredHeight);
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int maxTextWidth = image.getWidth();
        //generate image
        Color bgcolor = Color.WHITE;
        graphics.setColor(bgcolor);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        //add Logos
        try {
            maxTextWidth -= addRightLogo(width, height, image, graphics, bgcolor, logos.get("dfg"));
            maxTextWidth -= addLeftLogo(width, height, image, graphics, bgcolor, logos.get("thulb"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //write URN
        graphics.setPaint(Color.BLACK);
        graphics.setFont(font);
        drawStringCentered(graphics, width, height, printStr, lineMetric, lineWidth);
        return image;
    }

    private static int addRightLogo(final int width, int height, BufferedImage image, Graphics2D graphics, Color bgcolor, BufferedImage imageFile)
        throws IOException {
        BufferedImage logo = getLogo(imageFile, width / 5, height);
        int xPos = image.getWidth() - logo.getWidth();
        int yPos = (image.getHeight() - logo.getHeight()) / 2;
        graphics.drawImage(logo, xPos, yPos, logo.getWidth(), logo.getHeight(), bgcolor, null);
        return logo.getWidth();
    }

    private static int addLeftLogo(final int width, int height, BufferedImage image, Graphics2D graphics, Color bgcolor, BufferedImage imageFile)
        throws IOException {
        BufferedImage logo = getLogo(imageFile, width / 5, height);
        int xPos = 0;
        int yPos = (image.getHeight() - logo.getHeight()) / 2;
        graphics.drawImage(logo, xPos, yPos, logo.getWidth(), logo.getHeight(), bgcolor, null);
        return logo.getWidth();
    }

    /**
     * draws <code>textLines</code> horizontally and vertically aligned to center.
     * @param graphics where to draw Strings to
     * @param imageWidth image widths represented by graphics
     * @param imageHeight image height represented by graphics
     * @param textLines the String List with each line as a element
     * @param lineMetric lineMetrics of the complete text
     * @param textWidth the bounding box width of the text
     */
    private static void drawStringCentered(Graphics2D graphics, final int imageWidth, int imageHeight, List<String> textLines, LineMetrics lineMetric,
        double textWidth) {
        int x = (int) (imageWidth - textWidth) / 2;
        int y = (int) (imageHeight - textLines.size() * lineMetric.getHeight()) / 2;
        y += lineMetric.getHeight() / 2 + lineMetric.getDescent();
        for (int i = 0; i < textLines.size(); i++) {
            int liney = y + (i * (int) Math.ceil(lineMetric.getHeight()));
            graphics.drawString(textLines.get(i), x, liney);
        }
    }

    /**
     * reads a logo from <code>imgFile</code> and scales it down if needed.
     * @param imgFile the image
     * @param maxWidth maximum image width
     * @param maxHeight maximum image height
     * @return
     * @throws IOException if image could not be read
     */
    private static BufferedImage getLogo(BufferedImage image, int maxWidth, int maxHeight) throws IOException {
        if (image.getWidth() < maxWidth && image.getHeight() < maxHeight)
            return image;
        LOGGER.debug("Scale image");
        double yRatio = (double) maxHeight / image.getHeight();
        double xRatio = (double) maxWidth / image.getWidth();
        double scale = Math.min(xRatio, yRatio);
        int imageType = image.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_RGB;
        }
        int width = (int) (image.getWidth() * scale);
        int height = (int) (image.getHeight() * scale);
        final BufferedImage bicubic = new BufferedImage(width, height, imageType);
        final Graphics2D bg = bicubic.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        if (image.getColorModel().hasAlpha())
            bg.setComposite(AlphaComposite.Src);
        bg.scale(scale, scale);
        bg.drawImage(image, 0, 0, null);
        bg.dispose();
        return bicubic;
    }

    public static List<String> getStringForWidth(String orig, Font font, FontRenderContext frc, int width) {
        Rectangle2D stringBounds = font.getStringBounds(orig, frc);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(orig + " dimensions: " + (int) stringBounds.getWidth() + "x" + (int) stringBounds.getHeight() + ", max-width:" + width + ", lines:"
                + (int) Math.ceil(stringBounds.getWidth() / (double) width));
        }
        return rewriteString(orig, (int) Math.ceil(stringBounds.getWidth() / (double) width));
    }

    public static List<String> rewriteString(String orig, int lines) {
        ArrayList<String> strLines = new ArrayList<String>(lines);
        if (lines < 2) {
            strLines.add(orig);
            return strLines;
        }
        int lineLength = (int) Math.ceil(orig.length() / (double) lines);
        for (int i = 0; i < lines; i++) {
            strLines.add(orig.substring(i * lineLength, i + 1 < lines ? (i + 1) * lineLength : orig.length()));
        }
        return strLines;
    }

    private static BufferedImage readImage(InputStream imgFile) throws IOException {
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(imgFile);
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
        ImageReader imageReader = imageReaders.next();
        try {
            imageReader.setInput(imageInputStream);
            return imageReader.read(0);
        } finally {
            imageReader.dispose();
            imgFile.close();
        }
    }
}
