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
import java.util.Comparator; 
import java.util.Collections;
	
public class Triangulation {
	//Perform a triangulation using S-hull: a fast radial sweep-hull routine for Delaunay triangulation
	//source -> http://www.s-hull.org/paper/s_hull.pdf
	//Utilizing flip alogorithm
	//With furter Inspiration from Alexander Pletzer's code on Triangulation in Python

	ArrayList<Triangle> triangles;
	ArrayList<Vertex> points;
	ArrayList<Edge> perimeter;

	Vertex seed;
	Vertex c; 

	//Intialize points and create data structure to hold Triangles. 
	public Triangulation(int[][] p, int h){
		//Convert int[][] to ArrayList of Verticies;
		points = new ArrayList<Vertex>();
		for (int i = 0; i < p.length; i++) {
			points.add(new Vertex(p[i][1], (h - 1 - p[i][0])));
		}

		//Initalize Variables
		seed = new Vertex(-1,-1);
		c = new Vertex(-1,-1);
		triangles = new ArrayList<Triangle>();
		perimeter = new ArrayList<Edge>();
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
	public class C_comp implements Comparator<Vertex> {
		@Override
		public int compare(Vertex a, Vertex b) {
			double a1 = Math.pow(distance(a, c),2);
			double b1 = Math.pow(distance(b, c),2);
			if (a1 < b1) {
				return -1;
			} else if (b1 < a1) {
				return 1;
			} 
			return 0;
		}
	}
	public class Point_comp implements Comparator<Vertex> {
		@Override
		public int compare(Vertex a, Vertex b) {
			double a1 = Math.pow(distance(a, seed),2);
			double b1 = Math.pow(distance(b, seed),2);
			if (a1 < b1) {
				return -1;
			} else if (b1 < a1) {
				return 1;
			} 
			return 0;
		}
	}

	//####### METHODS ########//

	// Pick Seed point, then using seed point, pick xk and xj. Then using new triangle, find point C and order
	// the rest of points acording to distance from c. Finally, create triangulation using add_vertex for each point
	public ArrayList<Triangle> triangulate() {
		//randomly select seed point
		//Tesing sort
		seed = points.get((int) points.size() / 2);
		
		//Sort in distance closest to seed
		Comparator<Vertex> comp = new Point_comp();
		Collections.sort(points,comp);

		//Find initial Triangle with smallest circle
		Vertex xk = points.get(1);
		Vertex xj = points.get(2);
		double min = circum_radius(seed,xk,xj);
		for (int i = 3; i < 5; i++) {
			if (circum_radius(seed,xk,points.get(i)) < min) {
				xj = points.get(i);
				min = circum_radius(seed,xk,xj);
			}
		}
		//Add intialize triangles and Edges 
		Triangle first = new Triangle(seed,xk,xj);
		triangles.add(first);
		to_counterclockwise(first);		
		perimeter.add(new Edge(first.a,first.b));
		perimeter.add(new Edge(first.b,first.c));
		perimeter.add(new Edge(first.c,first.a));

		//Find center of first triangles circumcirlce which will be used to sort remaining points
		// c = circum_center(seed,xk,xj);
		// System.out.println("C point: " + c.x + ",  " +  c.y);
		
		// Comparator<Vertex> c_comp = new C_comp();
		// Collections.sort(points,c_comp);
		points.remove(seed);
		points.remove(xk);
		points.remove(xj);

		//Add Verticies to triangulation
		for(int i = 0; i < points.size(); i++) {
			add_vertex(points.get(i));
		}
		return triangles;
	}
	
	// System.out.println("seed is : (" + seed.x + ", " + seed.y + ")");
	// 	for (Vertex v : points) {
	// 		System.out.println("(" + v.x + ", " + v.y + ")");
	// }
	// System.out.println("Sorted..........");
	// 	for (Vertex v : points) {
	// 		System.out.println("(" + v.x + ", " + v.y + ")");
	// }

	//Add One Vertex to existing Delauney Triangulation, with array of Triangle
	public void add_vertex(Vertex p) {

		System.out.println("Perimeter size: " + perimeter.size());

		ArrayList<Edge> to_be_added = new ArrayList<Edge>();
		ArrayList<Edge> to_be_removed = new ArrayList<Edge>();

		for (Edge e : perimeter) {
			if (edge_visible(p, e)) {
				//Create new triangle, add to triangles.
				Triangle n_tri = new Triangle(e.a, e.b, p);
				to_counterclockwise(n_tri);
				triangles.add(n_tri);

				//update perimeter
				Edge e_a = new Edge(n_tri.c, n_tri.a);
				Edge e_b = new Edge(n_tri.b, n_tri.c);
				to_be_added.add(e_a);
				to_be_added.add(e_b);

				//remove visible edge
				to_be_removed.add(e);
			}
		}
		System.out.println("to be removed: " + to_be_removed.size());
		for (Edge e : to_be_removed) {
			perimeter.remove(e);
		}
		System.out.println("to be added: " + to_be_added.size());
		System.out.println("P size before addition: " + perimeter.size());
		for (int i = 0; i < to_be_added.size(); i ++) {
			Edge current = to_be_added.get(i);
			boolean unique = true;
			for (int j = 0; j < to_be_added.size(); j++) {
				if (i != j) {
					if (edge_e(current,to_be_added.get(j))){
						unique = false;
						break;
					}
				}
			}
			if (unique){
				perimeter.add(current);
			}
		}
		System.out.println("Perimeter size after addition: " + perimeter.size());
	}
	//Radius of the the circumcircle
	public double circum_radius(Vertex a, Vertex b, Vertex c) {
		double a1 = distance(a,b);
		double b1 = distance(b,c);
		double c1 = distance(c,a);
		double s = (a1 + b1 + c1) / 2;
		return (a1*b1*c1) / (4*Math.sqrt(s*(s-a1)*(s-b1)*(s-c1)));
	}
	//Center of the circumcirlcles
	public Vertex circum_center(Vertex a, Vertex b, Vertex c) {
		Vertex mid_ab = new Vertex((a.x + b.x)/2, (a.y + b.y)/2);
		Vertex mid_bc = new Vertex((b.x + c.x)/2, (b.y + c.y)/2);
		double sl_ab =  -1 /((double)(b.y - a.y) / (double)(b.x - a.x));
		double sl_bc =  -1 /((double)(c.y - b.y) / (double)(c.x - b.x));
		//y= sl_ab*x - sl_ab*midab.x + mid_ab.y 
		double x = ((double)((sl_bc*mid_bc.x) + mid_bc.y - mid_ab.y - (sl_ab*mid_ab.x))) / (double)(sl_ab - sl_bc);
		double y = sl_ab*x + sl_ab*mid_ab.x + mid_ab.y;
		return new Vertex((int)x,(int)y);
	}
	//Order Triangle Clockwise
	public void to_counterclockwise(Triangle t) {
		System.out.println(area(t.a,t.b,t.c));
		if (area(t.a,t.b,t.c) < 0) {
			//Clockwise, swap Vertices a and b
			Vertex temp = t.a;
			t.a = t.b;
			t.b = temp;
		}
		//Counter, keep as it is;
	}
	public double area(Vertex a, Vertex b, Vertex c) {
		return (b.x-a.x)*(c.y-a.y) - (c.x-a.x)*(b.y-a.y);
	}
	public boolean edge_visible(Vertex p, Edge e) {
		return (area(p, e.a, e.b) < 0);
	}
	public double distance(Vertex a, Vertex b) {
		return Math.sqrt(Math.pow((a.x - b.x), 2) + Math.pow((a.y - b.y),2));
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
