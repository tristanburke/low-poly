/**
 * Finds Priority Points of image and returns them in an array?
 * 
 * @author 	Tristan Burke
 * @version	1.0 - November 22, 2016
 * 
**/

import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.PriorityQueue; 

/** use a priority queque to hold points where pixels on either side differ in RGB values greatly
 *  i.e., find points that are on the border of two colors. Then, store said point in the Priority queue,
 *  with the associated value being the difference between the pixel and it's surround colors. i.e., how 
 *  much contrast at the border. This way we will select the points with the most contrast throughout the 
 *  entire picture. Lastly, we will have to add some constraint that does not permit consecutive or extremeley
 *  close pixels as then we could end up with our 50 pixels existing right next to each other (like on a single boarder). 
 **/

public class Priority_Points {

	public void main(String args[]) {

	}
}
