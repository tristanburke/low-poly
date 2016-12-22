/**
 * Main - Loads image, creates and returns low poly image
 * 
 * @author 	Tristan Burke
 * @version	1.2 - December 20, 2016
 * 
**/
package low_poly;

import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.util.PriorityQueue;
import java.util.Comparator; 

public class Low_poly {

	BufferedImage image;
	int number_of_points;
	int width;
	int height;
	int[][] pixels;
	Priority_Points priority_points;

	public Low_poly(BufferedImage i, int p) {
		image = i;
		number_of_points = p;
		priority_points = new Priority_Points();

		//convert buffered image into double array of pixels. 
		width = i.getWidth();
    	height = i.getHeight();
      	pixels = new int[height][width];


      	for (int row = 0; row < height; row++) {
         	for (int col = 0; col < width; col++) {
            	pixels[row][col] = i.getRGB(col, row);
         	}
      	}
      	System.out.print(pixels.length + ", " + pixels[0].length);

      	int[][] points = priority_points.triangulate(pixels, number_of_points);
      	for (int[] arr : points) {
      		System.out.print(arr[0] + ", " + arr[1]);
      	}
	}
	/** - Loads the file input in arguments, as well as a number that identifies how many points to be selected.
	  * - Converts the buffered image into double array of pixels. 
	  * - Then uses priority points file and triangulation file to finish low_poly process and the draws the image
	  * - back to output.
	**/
	public static void main(String[] args) {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		int number_of_points;

		if (args.length == 2) {
			try {
				image = ImageIO.read(new File(args[0]));
			} catch (IOException e) {
			}
			number_of_points = Integer.parseInt(args[1]);
		} else if (args.length == 1) {
			try {
				image = ImageIO.read(new File(args[0]));
			} catch (IOException e) {
			}
			number_of_points = 50;
		} else {
			throw new IllegalArgumentException("Incorrect number of arguments");	
		}
		Low_poly m = new Low_poly(image, number_of_points);
	}
}