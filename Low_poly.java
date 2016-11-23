/**
 * Main - Loads image, creates and returns low poly image
 * 
 * @author 	Tristan Burke
 * @version	1.0 - November 22, 2016
 * 
**/

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics;


public class Low_poly {

	BufferedImage image;
	int number_of_points;

	public Low_poly(BufferedImage i, int p) {
		image = i;
		number_of_points = p;
	}

	/** - Loads the file input in arguments, as well as a number that identifies how many points to be selected.
	  * - Then uses priority points file and triangulation file to finish low_poly process and the draws the image
	  * - back to output.
	**/
	public static void main(String[] args) {
		BufferedImage image;
		int number_of_points;

		if (args.length == 2) {
			try {
				image = ImageIO.read(new File(args[0]));
			} catch (IOException e) {
			}
			number_of_points = Integer.parseInt(args[1]);
		} else  if (args.length == 1) {
			try {
				image = ImageIO.read(new File(args[0]));
			} catch (IOException e) {
			}
			number_of_points = 50;
		} else {
			throw new IllegalArgumentException("Incorrect number of arguments");			
		}
		Graphics g = new Graphics();
		g.drawImage(image,0,0,null);
	}

}