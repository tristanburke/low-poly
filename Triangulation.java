/**
 * Takes a set of priority points and creates a triangulation from points and returns image
 * 
 * @author 	Tristan Burke
 * @version	1.0 - November 22, 2016
 * 
**/

package low_poly;

import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Triangulation {

	private List points;
	private Jpeg image;

	public Triangulation(Points p, JPEG i) {
		points = p;
		image = i;
	}

	private boolean within_triange(Pixel a, Pixel b, Pixel, c, Pixel d) {
		/** perform determininant of a,b,c,d matrix. when A,B, and C are sorted in counterclockwise order,
		 *  the determinant is positive if and only if d lies inside circumference.
		**/
	}
}
