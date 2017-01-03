/**
 * Takes a set of priority points and creates a triangulation from points and returns image
 * 
 * @author 	Tristan Burke
 * @version	1.3 - Jan 1, 2016
 * 
**/

package low_poly;

import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
	
public class Triangulation {

	ArrayList<Triangle> triangles;
	int[][] points;
	int width;
	int height;

	//Intialize points and create data structure to hold Triangles. 
	public Triangulation(int[][] p, int w, int h){
		points = p;
		width = w;
		height = h;
		triangles = new ArrayList<Triangle>();
	}

	//########## STRUCTURES ##########//
	//Structures includes Triangle -> 3 Edges -> 2 Verticies
	//A structure to represent a triangle on the image - contains 3 vertices
	public class Triangle {
		Vertex a;
		Vertex b;
		Vertex c;
		public Triangle(Vertex d, Vertex e, Vertex f) {
			a = d;
			b = e;
			c = f;
		}
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
	//Representing a triangle edge between two Vertices 
	public class Edge{
		public Vertex a;
		public Vertex b;

		public Edge(Vertex a1, Vertex b1) {
			a = a1;
			b = b1;
		}
	}

	//####### METHODS ########//

	//Form Triangles
	public ArrayList<Triangle> triangulate() {
		//Create supertriangle and add it to the triangle data structure
		Vertex a = new Vertex(-width,0);
		Vertex b = new Vertex(width, 0);
		Vertex c = new Vertex(0, 2*height);
		triangles.add(new Triangle(a,b,c));
		//For each vertex
		for (int i = 0; i < points.length; i++) {
			//Add vertex
			add_vertex(points[i][1], points[i][0]);
		}
		
		// For each Triangle see if one or more vertices stem from supertriangles
		ArrayList<Triangle> t_copy = new ArrayList<Triangle>(triangles);
		for (Triangle t: t_copy) {
			if (vertex_e(t.a,a) || vertex_e(t.b,a) || vertex_e(t.c,a)
				|| vertex_e(t.a, b) || vertex_e(t.b, b) || vertex_e(t.c, b) 
				|| vertex_e(t.a, c) || vertex_e(t.b, c) || vertex_e(t.c, c)){
				triangles.remove(t);
			}
		}
		return triangles;
	}
	

	//Add One Vertex to existing Delauney Triangulation, with array of Triangle
	public void add_vertex(int x, int y) {


		Vertex p = new Vertex(x, y);
		ArrayList<Triangle> t_copy = new ArrayList<Triangle>(triangles);
		ArrayList<Edge> edgebuffer = new ArrayList<Edge>();
		
		System.out.println("Size before: " + triangles.size());
		for (Triangle t : t_copy) {
			Vertex a;
			Vertex b = t.b;
			Vertex c;
			//Sort into counterclockwise order
			int order  = (t.b.y - t.a.y)*(t.c.x - t.b.x)-(t.c.y-t.b.y)*(t.b.x - t.a.x);
			if (order == 0) {
				break;
			}
			if (order < 0 ) {
				a = t.a;
				c = t.c;
			} else {
				a = t.c;
				c = t.a;
			}
			if (within_triangle(a,b,c,p)) {
				//store Triangles's edges in edgebuffer
				System.out.println("test");
				edgebuffer.add(new Edge(t.a, t.b));
				edgebuffer.add(new Edge(t.b, t.c));
				edgebuffer.add(new Edge(t.c, t.a));
				//remove Triangle 
				triangles.remove(t);
			}
		}
		System.out.println("Size after: " + triangles.size());
		//remove all double edges from edgebuffer, keeping only the unique ones
		ArrayList<Edge> e_unique = new ArrayList<Edge>();
		for (int i = 0; i < edgebuffer.size(); i++) {
			Boolean unique = true;
			Edge current = edgebuffer.get(i);
			for (int j = 0; j < edgebuffer.size(); j++) {
				if (j != i) {
					Edge other = edgebuffer.get(j);
					if (edge_e(other, current)) {
						unique = false;
						break;
					}
				}
			}
			if (unique) {
				e_unique.add(current);
			}
		}
		//for a new triangle between edge and vertex
		for (Edge e : e_unique) {
			Triangle current = new Triangle(e.a, e.b, p);
			triangles.add(current);
		}
		System.out.println("Number of Triangles added: " + e_unique.size());
	}
	public void test() {
		// Vertex a = new Vertex(0,0);
		// Vertex b = new Vertex(0,10);
		// Vertex c = new Vertex(10,0);
		// Vertex d = new Vertex(25,25);
		// boolean test = within_triangle(c,b,a,d);
		// if (test) {
		// 	System.out.println("fail");
		// } else {
		// 	System.out.println("not");
		// }
	}
	public boolean vertex_e(Vertex a, Vertex b) {
		return (a.x == b.x && a.y == b.y);
	}
	public boolean edge_e(Edge a, Edge b) {
		return ((vertex_e(a.a, b.a) && vertex_e(a.b, b.b)) || (vertex_e(a.b, b.a) && vertex_e(a.a, b.b)));
	}
	// Determines whether or not point D lies within the triangle formed be A, B, C
	// This is done by calculating the determinant of a specific matric defined by
	// A,B,C, and D's x and y positions. If this determinant is > 0, it lies within.
	// Futhermore, A, B, and C must be ordered counter-clockwise
	public boolean within_triangle(Vertex a1, Vertex b1, Vertex c1, Vertex d) {
		// Assume a1, b1, and c1 are passed in counterclockwise
		// Matrix Elements
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
		double det = a11*((a22*a33)-(a23*a32)) - a12*((a21*a33)-(a23*a31)) + a13*((a21*a32)-(a22*a31)); 
		return (det > 0);
	}
}
