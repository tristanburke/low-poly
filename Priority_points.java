/**
 * Finds Priority Points of image and returns them in an Array
 * 
 * @author 	Tristan Burke
 * @version	1.2 - December 20, 2016
 * 
**/

import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.PriorityQueue;
import java.util.Comparator; 

/** use a priority queque to hold points where pixels on either side differ in RGB values greatly
 *  i.e., find points that are on the border of two colors. Then, store said point in the Priority queue,
 *  with the associated value being the difference between the pixel and it's surround colors. i.e., how 
 *  much contrast at the border. This way we will select the points with the most contrast throughout the 
 *  entire picture. Lastly, we will have to add some constraint that does not permit consecutive or extremeley
 *  close pixels as then we could end up with our 50 pixels existing right next to each other (like on a single boarder). 
 **/

public class Priority_Points {


	public int[][] triangulate(int[][] pixels, int number_of_points) {
		Point[][] points = new Point[pixels.length][pixels[0].length];

		for (i = 0; i < pixels.length; i++) {
			for (j = 0; j < pixels[0].length; j++) {
				int current = pixels[i][j]
				int above = 0;
				int below = 0;
				int left = 0;
				int right = 0;
				if (i != 0) {
					above = Math.abs(current - pixels[i-1][j]);
				}
				if (i != pixels.length) {
					below = Math.abs(current - pixels[i+1][j]);
				}
				if (j != 0) {
					left = Math.abs(current - pixels[i][j-1]);
				}
				if (j != pixels[0].length) {
					right = Math.abs(current - pixels[i][j+1]);
				}
				int contrast = Math.max(above,below,right,left);
				Point n = new Point(i, j, contrast);
				points[i][j] = n;
			}
		}
		Comparator<Point> comp = new PointComparator();
		PriorityQueue<Point> queue = new PriorityQueue<Point>(comp, number_of_points);



	}

	public class Point {
		int row;
		int col;
		int contrast;
		public Point(int a, int b, int c) {
			row = a;
			col = b;
			contrast = c;
		}
	}

	public class PointComparator implements Comparator<Point> {
    	@Override
    	public int compare(Point a, Point b) {

        	if (x.length() < y.length()) {	
            	return -1;
        	} if (x.length() > y.length()) {
            	return 1;
        	}
        	return 0;
    	}
	}
}
