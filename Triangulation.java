/**
 * Takes a set of priority points and creates a triangulation from points and returns image
 * 
 * @author 	Tristan Burke
 * @version	2.1 - Jan 5, 2016
 * 
**/

package low_poly;

import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator; 
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
	
public class Triangulation {
	//Perform a triangulation using S-hull: a fast radial sweep-hull routine for Delaunay triangulation
	//source -> http://www.s-hull.org/paper/s_hull.pdf
	//Utilizing flip alogorithm
	//With furter Inspiration from Alexander Pletzer's code on Triangulation in Python

	HashMap<int,Triangle> triangles;
	ArrayList<Vertex> points;
	ArrayList<Edge> perimeter;
	HashMap<Edge,int[]> edge_triangles;

	Vertex seed;
	Vertex c; 

	int index;

	//Intialize points and create data structure to hold Triangles. 
	public Triangulation(int[][] p, int h){
		//Convert int[][] to ArrayList of Verticies;
		points = new ArrayList<Vertex>();
		for (int i = 0; i < p.length; i++) {
			points.add(new Vertex(p[i][1], (h - 1 - p[i][0])));
		}
		remove_duplicates();

		//Initalize Variables
		seed = new Vertex(-1,-1);
		c = new Vertex(-1,-1);
		triangles = new ArrayList<Triangle>();
		perimeter = new ArrayList<Edge>();
		edge_triangles = new HashMap<Edge, Triangle[]>();
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
		@Override
		public int hashCode(){
			return x * 31 + y;
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return edge_e(this, (Edge) o);
		}
		@Override
		public int hashCode() {
			int result = a != null ? a.hashCode() : 0;
			result = 31 * result + (b != null ? b.hashCode() : 0);
			return result;
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
		to_counterclockwise(first);	
		triangles.add(first);

		//Set inital perimeter
		Edge e_a = new Edge(first.a,first.b);
		Edge e_b = new Edge(first.b,first.c);
		Edge e_c = new Edge(first.c,first.a);	
		perimeter.add(e_a);
		perimeter.add(e_b);
		perimeter.add(e_c);

		//Intialize their Edge-> triangles arrays
		Triangle[] first_a = new Triangle[2];
		first_a[0] = first;
		edge_triangles.put(e_a, first_a);
		edge_triangles.put(e_b, first_a.clone());
		edge_triangles.put(e_c, first_a.clone());

		//Find center of first triangles circumcirlce which will be used to sort remaining points
		// c = circum_center(seed,xk,xj);
		// System.out.println("C point: " + c.x + ",  " +  c.y);
		
		// Comparator<Vertex> c_comp = new C_comp();
		// Collections.sort(points,c_comp);
		points.remove(seed);
		points.remove(xk);
		points.remove(xj);

		//Add Verticies to triangulation
		for(int i = 0; i < 21; i++) {
			System.out.println(points.get(i).x + ", " + points.get(i).y);
			add_vertex(points.get(i));
		}
		return triangles;
	}
	// public void test(){
	// 	Vertex a = new Vertex(0,0);
	// 	Vertex b = new Vertex(10, 0);
	// 	Vertex c = new Vertex(0,5);
	// 	System.out.println(angle(a,b,c));
	// 	System.out.println(angle(b,c,a));
	// 	System.out.println(angle(c,a,b));
	// }
	//Add One Vertex to existing Delauney Triangulation, with array of Triangle
	public void add_vertex(Vertex p) {

		ArrayList<Edge> to_be_added = new ArrayList<Edge>();
		ArrayList<Edge> to_be_removed = new ArrayList<Edge>();

		for (Edge e : perimeter) {
			if (edge_visible(p, e)) {
				//Create new triangle, add to triangles.
				Triangle n_tri = new Triangle(e.a, e.b, p);
				to_counterclockwise(n_tri);
				triangles.add(n_tri);

				//update the already existing edge to have the new triangle;
				Triangle[] array = edge_triangles.get(e);
				array[1] = n_tri;
				edge_triangles.put(e, array);

				//update perimeter
				Edge e_a = new Edge(n_tri.c, n_tri.a);
				Edge e_b = new Edge(n_tri.b, n_tri.c);
				to_be_added.add(e_a);
				to_be_added.add(e_b);

				//Intialize their triangles 
				Triangle[] e_a_array = new Triangle[2];
				Triangle[] e_b_array = new Triangle[2];
				e_a_array[0] = n_tri;
				e_b_array[0] = n_tri;
				edge_triangles.put(e_a, e_a_array);
				edge_triangles.put(e_b, e_b_array);
				System.out.println("Added: (" + e_a.a.x + ", " + e_a.a.y + ") ----> (" + e_a.b.x + ", " + e_a.b.y + ")");
				System.out.println("Added: (" + e_b.a.x + ", " + e_b.a.y + ") ----> (" + e_b.b.x + ", " + e_b.b.y + ")");
				//remove visible edge
				to_be_removed.add(e);
			}
		}
		//Remove Perimeter edges no longer on perimeter
		for (Edge e : to_be_removed) {
			perimeter.remove(e);
		}
		//Add edges to Perimeter that are unique
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
		flip();
	}
	//Delauney Flip Method
	public void flip() {
		ArrayList<Edge> edges = new ArrayList<Edge>(edge_triangles.keySet());

		Boolean done = false; 
		while (!done) {
			ArrayList<Edge> ret_edges = new ArrayList<Edge>();
			for (Edge e : edges) {
				ArrayList<Edge> to_flip = flip_edge(e);
				for (Edge e2 : to_flip) {
					ret_edges.add(e2);
				}
			}
			edges = ret_edges;
			done = edges.size() == 0;
		}

	}
	public ArrayList<Edge> flip_edge(Edge e) {
		ArrayList<Edge> new_edges = new ArrayList<Edge>();

		Triangle[] e_triangles = edge_triangles.get(e);
		if (e_triangles == null) {
			e_triangles = edge_triangles.get(new Edge(e.b, e.a));
		}
		if (e_triangles == null || e_triangles[0] == null || e_triangles[1] == null) {
			return new_edges;
		}
		//Get two Triangles Edge is part of
		Triangle tri_a = e_triangles[0];
		Triangle tri_b = e_triangles[1];

		//Find vertices opposite the edge for each triangle
		Vertex opp_a = find_opp(tri_a,e);
		Vertex opp_b = find_opp(tri_b,e);

		//Calculate angles at opposite vertices
		double angle_a = angle(opp_a, e.a, e.b);
		double angle_b = angle(opp_b, e.a, e.b);
		//If sum of angles <= 180, valid trianlge, if not, flip edge
		if (angle_a + angle_b > 3.1415926536) {
			//Create new triangles
			Triangle n_tri_a = new Triangle(opp_a, e.a, opp_b);
			Triangle n_tri_b = new Triangle(opp_a, opp_b, e.b);
			to_counterclockwise(n_tri_a);
			to_counterclockwise(n_tri_b);

			//Add new Triangles
			triangles.add(n_tri_a);
			triangles.add(n_tri_b);

			//remove old edge
			edge_triangles.remove(e);
			System.out.println("Removed: (" + e.a.x + ", " + e.a.y + ") ----> (" + e.b.x + ", " + e.b.y + ")");

			//add new Edge
			Triangle[] t = {n_tri_a,n_tri_b};
			Edge flipped_e = new Edge(opp_a, opp_b);
			edge_triangles.put(flipped_e,t);
			System.out.println("Flip Added: (" + flipped_e.a.x + ", " + flipped_e.a.y + ") ----> (" + flipped_e.b.x + ", " + flipped_e.b.y + ")");

			//Modify triangle's other edges' edge_triangles
			ArrayList<Edge> six_edges = new ArrayList<Edge>();
			six_edges.add(new Edge(n_tri_a.a, n_tri_a.b));
			six_edges.add(new Edge(n_tri_a.b, n_tri_a.c));
			six_edges.add(new Edge(n_tri_a.c, n_tri_a.a));
			six_edges.add(new Edge(n_tri_b.a, n_tri_b.b));
			six_edges.add(new Edge(n_tri_b.b, n_tri_b.c));
			six_edges.add(new Edge(n_tri_b.c, n_tri_b.a));
			for (int i = 0; i < 6; i++) {
				Edge current = six_edges.get(i);
				if (!edge_e(flipped_e,current)) {
					new_edges.add(current);
					Triangle[] ts = edge_triangles.get(current);
					if (ts == null) {
						ts = edge_triangles.get(new Edge(current.b, current.a));
					}
					if (ts == null) {
						System.out.println("No edge Triangle : " + current.a.x + ", " + current.a.y + " -> " + current.b.x + ", " + current.b.y);
						System.out.println("New");
						print_triangle(n_tri_a);
						print_triangle(n_tri_b);
						System.out.println("Old");
						print_triangle(tri_a);
						print_triangle(tri_b);
					}
					if (edge_in_triange(current, n_tri_a)) {
						if (ts[0] == tri_a || ts[0] == tri_b) {
							ts[0] = n_tri_a;
						} else {
							ts[1] = n_tri_a;
						}
					} else {
						if (ts[0] == tri_a || ts[0] == tri_b) {
							ts[0] = n_tri_b;
						} else {
							ts[1] = n_tri_b;
						}
					}
					edge_triangles.put(current,ts);
				}
			}
			//Remove old triangles
			triangles.remove(tri_a);
			triangles.remove(tri_b);
		}
		return new_edges;

	}
	//Find the Vertex in the triangle not in the edge
	public Vertex find_opp(Triangle t, Edge e) {
		if (vertex_e(t.a, e.a) || vertex_e(t.a, e.b)) {
			if (vertex_e(t.b, e.a) || vertex_e(t.b, e.b)) {
				return t.c;
			} else {
				return t.b;
			}
		} else {
			return t.a;
		}
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
		if (area(t.a,t.b,t.c) < 0) {
			//Clockwise, swap Vertices a and b
			Vertex temp = t.a;
			t.a = t.b;
			t.b = temp;
		}
		//Counter, keep as it is;
	}
	public double angle(Vertex vertex, Vertex a, Vertex b){
		Vertex vA = new Vertex((a.x - vertex.x),(a.y - vertex.y));
		Vertex vB = new Vertex((b.x - vertex.x),(b.y - vertex.y));
		double cp = area(vertex, a, b);
		double dp = dot(vA, vB);
		return Math.abs(Math.atan2(cp,dp));
	}
	public boolean edge_in_triange(Edge e, Triangle t) {
		return (edge_e(e, new Edge(t.a,t.b)) || edge_e(e, new Edge(t.b,t.c)) || edge_e(e, new Edge(t.c,t.a)));
	}
	public void test_print_edge_to_triangles() {
		Set<Edge> test = edge_triangles.keySet();
		for (Edge e : test) {
			Triangle[] tris = edge_triangles.get(e);
			System.out.println("Edge: " + e.a.x + ", " + e.a.y + " -> " + e.b.x + ", " + e.b.y);
			print_triangle(tris[0]);
			if (tris[1] != null) {
				print_triangle(tris[1]);
			}
		}

	}
	public void remove_duplicates() {
		ArrayList<Vertex> to_be_removed = new ArrayList<Vertex>();
		for (int i = 0; i < points.size(); i++) {
			Vertex current = points.get(i);
			for (int j = 0; j < points.size(); j++) {
				if (i != j) {
					if (vertex_e(current, points.get(j))) {
						to_be_removed.add(current);
					}
				}
			}
		}
		for (Vertex v : to_be_removed) {
			points.remove(v);
		}
	}
	public double dot(Vertex a, Vertex b) {
		return ((a.x*b.x) + (a.y*b.y));
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
	public void print_triangle(Triangle t) {
		System.out.println("(" + t.a.x + ", " + t.a.y + ") " + " (" + t.b.x + ", " + t.b.y + ") "+ "(" + t.c.x + ", " + t.c.y + ") ");
	}
}
