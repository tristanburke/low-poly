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

	Triangle[] triangles;
	int[][] points;

	//Intialize points and create data structure to hold Triangles. 
	public Triangulation(int[][] p, ){
		points = p;
	}
	//Form Triangles
	public Triangle[] triangulate() {

		for (int i = 0; i < points.length; i++) {

		}

	}
	//Add One Vertex to existing Delauney Triangulation, with array of Triangle
	public void add_vertex() {
		for (each triange) {
			if (within_triangle()) {
				//store Triangles's edges in edgebuffer
				//remove Triangle 
			}
		}
		//remove all double edges from edgebuffer, keeping only the unique ones
		for (each edge in edgebuffer) {
			//for a new triangle between edge and vertex
		}
	}
	//A structure to represent a triangle on the image - contains 3 vertices
	public class Triangle {
		Edge a;
		Edge b;
		Edge c;
		public Triangle(Edge d, Edge e, Edge f) {
			a = d;
			b = e;
			c = f;
		}
	}
	// Determines whether or not point D lies within the triangle formed be A, B, C
	// This is done by calculating the determinant of a specific matric defined by
	// A,B,C, and D's x and y positions. If this determinant is > 0, it lies within.
	// Futhermore, A, B, and C must be ordered counter-clockwise
	public boolean within_triangle(Vertex a1, Vertex b1, Vertex c1, Vertex d) {
		// Assume a1, b1, and c1 are passed in in clockwise
		//Matrix Elements
		int a11 = a1.x - d.x;
		int a21 = b1.x - d.x;
		int a31 = c1.x - d.x;
		int a12 = a1.y - d.y;
		int a22 = b1.y - d.y;
		int a32 = c1.y - d.y;
		double a13 = (Math.pow(a1.x, 2) - Math.pow(d.x, 2)) + (Math.pow(a1.y, 2) - Math.pow(d.y, 2));
		double a23 = (Math.pow(b1.x, 2) - Math.pow(d.x, 2)) + (Math.pow(b1.y, 2) - Math.pow(d.y, 2));
		double a33 = (Math.pow(c1.x, 2) - Math.pow(d.x, 2)) + (Math.pow(c1.y, 2) - Math.pow(d.y, 2));
		// Calculate Determinant 
		double det = a11*((a22*a33) - (a23*a32)) - a12*((a21*a33) - (a23*a31)) + a13*((a21*a32) - (a22*a31)); 
		
		return (det > 0);
	}
	//Representing a Vertex with coordinates x and y
	public class Vertex {
		public int x;
		public int y;
		
		public Vertex(int a, int b) {
			x = a;
			y = b;
		}
	}
	public class Edge{
		public Vertex a;
		public Vertex b;

		public Edge(Vertex a1, Vertex b1) {
			a = a1;
			b = b1;
		}
	}
}
