/**
 * Finds Priority Points of image and returns them in an Array
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

	public Priority_Points() {
	}

	public int[][] triangulate(int[][] pixels, int number_of_points) {

		Comparator<Point> comp = new PointComparator();
		PriorityQueue<Point> queue = new PriorityQueue<Point>(number_of_points, comp);

		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[0].length; j++) {
				int current = pixels[i][j];
				int above = 0;
				int below = 0;
				int left = 0;
				int right = 0;
				if (i != 0) {
					above = Math.abs(current - pixels[i-1][j]);
				}
				if (i < pixels.length - 1) {
					below = Math.abs(current - pixels[i+1][j]);
				}
				if (j != 0) {
					left = Math.abs(current - pixels[i][j-1]);
				}
				if (j < pixels[0].length - 1) {
					right = Math.abs(current - pixels[i][j+1]);
				}
				int contrast = Math.max(Math.max(above,below),Math.max(right,left));
				Point n = new Point(i, j, contrast);
				queue.add(n);
				if (queue.size() > number_of_points) {
					queue.poll();
				}
			}
		}
		//Convert the priority points into an int[][] where each elemenet in arra
		Point[] points = queue.toArray(new Point[0]);
		int[][] ret = new int[points.length][2];
		for (int i = 0; i < points.length; i++) {
			ret[i][0] = points[i].row;
			ret[i][1] = points[i].col;
		}
		return ret;
	}

	public class Point {
		public int row;
		public int col;
		public int contrast;

		public Point(int a, int b, int c) {
			row = a;
			col = b;
			contrast = c;
		}

	}

	public class PointComparator implements Comparator<Point> {
    	@Override
    	public int compare(Point a, Point b) {

        	if (a.contrast < b.contrast) {	
            	return -1;
			}
			if (a.contrast > b.contrast) {
            	return 1;
        	}
        	return 0;
    	}
	}
}
