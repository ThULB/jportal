package fsu.jportal.it;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PaintTestPics {	
	
	public BufferedImage oneColorPic(int width, Color color) {
		BufferedImage img = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D pic2D = img.createGraphics();
		pic2D.setPaint(color);
		pic2D.fillRect(0, 0, img.getWidth(), img.getHeight());
		pic2D.dispose();
		
		return img;
	}

	public BufferedImage threeColorPic(int width) {
		BufferedImage img = oneColorPic(10000, Color.RED);
		
		Graphics2D pic2D = img.createGraphics();
		int position = (int) (width * 0.25);
		pic2D.drawImage(oneColorPic((int) (width / 2), Color.GREEN), position, position, null);
		position += (int) ((width / 2) * 0.25);
		pic2D.drawImage(oneColorPic((int) (width / 4), Color.BLUE), position, position, null);
		
		return img;
	}
}
