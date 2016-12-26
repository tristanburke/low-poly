/**
 * Main - Loads image, creates and returns low poly image
 * 
 * @author 	Tristan Burke
 * @version	1.2 - December 20, 2016
 * 
**/
package low_poly;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.PriorityQueue;
import java.util.Comparator; 

public class Low_poly {

	BufferedImage image;
	String name;
	int number_of_points;
	int width;
	int height;
	int[][] pixels;
	Priority_Points priority_points;

	public Low_poly(BufferedImage img, String n, int p) {
		image = img;
		name = n;
		number_of_points = p;
		priority_points = new Priority_Points();

		//convert buffered image into double array of pixels. 
		width = img.getWidth();
    	height = img.getHeight();
      	pixels = new int[height][width];


      	for (int row = 0; row < height; row++) {
         	for (int col = 0; col < width; col++) {
            	pixels[row][col] = img.getRGB(col, row);
         	}
      	}
      	System.out.println("Image Width: " + pixels.length + ", " + pixels[0].length);

      	int[][] points = priority_points.triangulate(pixels, number_of_points);
      	System.out.println("Triangulate return length: " + points.length);

      	//Draw Returned Image
      	BufferedImage finished_image = copy(image);
      	for (int i = 0; i < points.length; i++) {
      		int x = points[i][1];
      		int y = points[i][0];
      		finished_image.setRGB(x, y, 16711680);
      	}
      	save(finished_image, name);
	}
	public static BufferedImage copy(BufferedImage source) {
 		ColorModel cm = source.getColorModel();
 		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
 		WritableRaster raster = source.copyData(null);
 		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	public static void display(BufferedImage image) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public static void save(BufferedImage image, String name) {
		try {
    		// retrieve image
    		File outputfile = new File(name + ".png");
    		ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
		}
	}
	public static void main(String[] args) {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		int number_of_points;
		String name;
		if (args.length == 3) {
			try {
				image = ImageIO.read(new File(args[0]));
			} catch (IOException e) {
			}
			name = args[1];
			number_of_points = Integer.parseInt(args[2]);
		} else if (args.length == 2) {
			try {
				image = ImageIO.read(new File(args[0]));
			} catch (IOException e) {
			}
			name = args[1];
			number_of_points = 50;
		} else {
			throw new IllegalArgumentException("Incorrect number of arguments");	
		}
		Low_poly m = new Low_poly(image, name, number_of_points);
	}
}