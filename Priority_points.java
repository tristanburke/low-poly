/**
 * Finds Priority Points of image and returns them in an Array
 * 
 * @author 	Tristan Burke
 * @version	1.3 - December 20, 2016
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

	public int[][] get_points(int[][] pixels, int number_of_points) {

		//Create Priority Queue to hold points
		Comparator<Point> comp = new PointComparator();
		PriorityQueue<Point> queue = new PriorityQueue<Point>(number_of_points, comp);

		//Calculate Correct Block Size based off of the number of points desired, and image size. 

		//Break Image into Blocks. Find point of highest 
		int wb = (int) pixels.length / 12;
		int hb = (int) pixels[0].length / 12;
		for (int i = 0; i < pixels.length; i = i + wb) {
			for (int j = 0; j < pixels[0].length; j = j + hb) {

				int max_contrast = 0;
				Point max_point = new Point(0,0,0);
				//Iterate through Block to find Best Pixel
				for (int inner_i = i; inner_i < i + wb && inner_i < pixels.length; inner_i++) {
					for (int inner_j = j; inner_j < j + wb && inner_j < pixels[0].length; inner_j++) {
						int current = pixels[inner_i][inner_j];
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
						if (contrast > max_contrast) {
							max_contrast = contrast;
							max_point = new Point(inner_i, inner_j, contrast);
						}
					}
				}
				queue.add(max_point);
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
