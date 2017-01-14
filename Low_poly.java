/**
 * Main - Loads image, creates and returns low poly image
 * 
 * @author 	Tristan Burke
 * @version	1.3 - Jan 1, 2016
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
import java.util.ArrayList;

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
      	System.out.println("Image Width: " + width);
      	System.out.println("Image Height: " + height);

      	int[][] points = priority_points.get_points(pixels, number_of_points);
      	System.out.println("Number of Points: " + points.length);
      	
      	//Test Priority Points
      	BufferedImage finished_image = copy(image);
      	for (int i = 0; i < points.length; i++) {
      		int x = points[i][1];
      		int y = points[i][0];
      		color_test(finished_image, x, y, 16711680, width, height);
      	}
     	save(finished_image, "Points");
      	
      	// Triangulate

        //Testing set of points 
      	Triangulation t = new Triangulation(points,height,width, image);
      	ArrayList<Triangulation.Triangle> triangles = t.triangulate();
      	ArrayList<Triangulation.Vertex> uncovered = t.find_empty_vertices();

     	//Test triangles 
     	System.out.println("Number of Triangles: " + triangles.size());
     	BufferedImage t_image = copy(image);
     	Graphics2D g2d = t_image.createGraphics();
     	g2d.setColor(Color.BLACK);
        BasicStroke bs = new BasicStroke(2);
        g2d.setStroke(bs);

      	for (int i = 0; i < triangles.size(); i++) {
      		Triangulation.Triangle current = triangles.get(i);
      		int ax = current.a.x;
      		int ay = -current.a.y + height - 1;
      		int bx = current.b.x;
      		int by = -current.b.y + height - 1;;  
      		int cx = current.c.x;
      		int cy = -current.c.y + height - 1;;  

      		int random = (int)(Math.random() * 250 + 1);

      		int[] xpoints = {ax, bx, cx};
      		int[] ypoints = {ay, by, cy};
      		g2d.setColor(new Color(0, 0, random));
      		g2d.fillPolygon(xpoints, ypoints, 3);

      		g2d.drawLine(ax, ay, bx, by);
      		g2d.drawLine(bx, by, cx, cy);
      		g2d.drawLine(cx, cy, ax, ay);
   			
      	}
      	//color holes
      	Triangulation.Vertex last = uncovered.get(0);
      	int color = 0;
      	for (int i = 1; i < uncovered.size(); i++) {
      		Triangulation.Vertex current = uncovered.get(i);
      		if (current.x - last.x > 2 && current.y - last.y > 2) {
      			color = color + i;
      			t_image.setRGB(current.x, -current.y + height - 1, color);
      		} else {
      			t_image.setRGB(current.x, -current.y + height - 1, color);
      		}
      		last = current;

      	}
      	save(t_image , "Triangles");

      	//Draw Finished Image
	}
	public static void color_test(BufferedImage source, int x, int y, int color, int width, int height) {
		source.setRGB(x, y, color);
  		if (x < width-1) {
  			source.setRGB(x+1, y, color);
  		}
  		if (x != 0) {
  			source.setRGB(x-1, y, color);
  		}
  		if (y < height-1) {
  			source.setRGB(x, y+1, color);
  		}	
  		if (y != 0) {
  			source.setRGB(x, y-1, color);
  		}
	}
	public static BufferedImage copy(BufferedImage source) {
 		ColorModel cm = source.getColorModel();
 		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
 		WritableRaster raster = source.copyData(null);
 		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
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