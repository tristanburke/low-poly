/**
 * Takes a set of priority points and creates a triangulation from points and returns image
 * 
 * @author 	Tristan Burke
 * @version	3.1 - Jan 12, 2016
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
	//source of algorithm -> http://www.s-hull.org/paper/s_hull.pdf
	//Utilizing flip alogorithm
	//With furter Inspiration from Alexander Pletzer's code on Triangulation in Python

	ArrayList<Triangle> triangles;
	ArrayList<Vertex> points;
	ArrayList<Edge> perimeter;
	HashMap<Edge,Integer[]> edge_triangles;

	Vertex cg;
	Vertex seed;
	Vertex hole;

	int width;
	int height;
	BufferedImage image;

	//Intialize points and create data structure to hold Triangles. 
	public Triangulation(int[][] p, int h, int w, BufferedImage img){
		width = w;
		height = h;
		image = img;

		//Convert int[][] to ArrayList of Verticies;
		points = new ArrayList<Vertex>();
		for (int i = 0; i < p.length; i++) {
			points.add(new Vertex(p[i][1], (h - 1 - p[i][0])));
		}
		//Add four corners of image to points
		points.add(new Vertex(0,0));
		points.add(new Vertex(0,h-1));
		points.add(new Vertex(w-1,0));
		points.add(new Vertex(w-1,h-1));
		//Removed duplicate points
		remove_duplicates();

		//Initalize Variables
		cg = new Vertex(-1, -1);
		seed = new Vertex(-1,-1);
		hole = new Vertex(-1, -1);
		triangles = new ArrayList<Triangle>();
		perimeter = new ArrayList<Edge>();
		edge_triangles = new HashMap<Edge, Integer[]>();
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
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return vertex_e(this, (Vertex) o);
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
	public class Hole_comp implements Comparator<Vertex> {
		@Override
		public int compare(Vertex a, Vertex b) {
			double a1 = Math.pow(distance(a, hole),2);
			double b1 = Math.pow(distance(b, hole),2);
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
			double a1 = Math.pow(distance(a, cg),2);
			double b1 = Math.pow(distance(b, cg),2);
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

		//Compute center of points. Pick seed as closed to center. 
		compute_center();
		Comparator<Vertex> comp = new Point_comp();
		Collections.sort(points, comp);
		seed = points.get(0);

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
		Integer[] first_a = new Integer[2];
		first_a[0] = 0;
		edge_triangles.put(e_a, first_a);
		edge_triangles.put(e_b, first_a.clone());
		edge_triangles.put(e_c, first_a.clone());

		points.remove(seed);
		points.remove(xk);
		points.remove(xj);

		//Add Verticies to triangulation
		for(int i = 0; i < points.size(); i++) {
			add_vertex(points.get(i));
		}
		flip();
		return triangles;
	}

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
				Integer triangle_index = triangles.size() - 1;

				//update the already existing edge to have the new triangle;
				Integer[] array = edge_triangles.get(e);
				array[1] = triangle_index;
				edge_triangles.put(e, array);

				//update perimeter
				Edge e_a = new Edge(n_tri.c, n_tri.a);
				Edge e_b = new Edge(n_tri.b, n_tri.c);
				to_be_added.add(e_a);
				to_be_added.add(e_b);

				//Intialize their triangles 
				Integer[] e_a_array = new Integer[2];
				Integer[] e_b_array = new Integer[2];
				e_a_array[0] = triangle_index;
				e_b_array[0] = triangle_index;
				edge_triangles.put(e_a, e_a_array);
				edge_triangles.put(e_b, e_b_array);

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

		Integer[] e_triangles = edge_triangles.get(e);
		if (e_triangles == null) {
			e_triangles = edge_triangles.get(new Edge(e.b, e.a));
		}
		if (e_triangles == null || e_triangles[1] == null) {
			return new_edges;
		}
		//Get two Triangles Edge is part of
		Integer index_a = e_triangles[0];
		Integer index_b = e_triangles[1];
		Triangle tri_a = triangles.get(index_a);
		Triangle tri_b = triangles.get(index_b);

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

			//Replace old triangles with new ones
			triangles.set((int)index_a, n_tri_a);
			triangles.set((int)index_b, n_tri_b);

			//remove old edge
			edge_triangles.remove(e);

			//add new Edge
			Edge flipped_e = new Edge(opp_a, opp_b);
			Integer[] tris =  {index_a, index_b};
			edge_triangles.put(flipped_e, tris);

			//Modify triangle's other edges' edge_triangles
			Edge a = new Edge(opp_a,e.b);
			Integer[] mod_a = edge_triangles.get(a);
			if (mod_a == null) {
				mod_a = edge_triangles.get(new Edge(e.b, opp_a));
			}
			if (mod_a != null) {
				for (int i = 0; i < 2; i++) {
					if (mod_a[i] == index_a) {
						mod_a[i] = index_b;
					}
				}
			}
			new_edges.add(a);

			Edge b = new Edge(opp_b, e.a);
			Integer[] mod_b = edge_triangles.get(b);
			if (mod_b == null) {
				mod_b = edge_triangles.get(new Edge(e.a, opp_b));
			}
			if (mod_b == null) {
				test_print_edge_to_triangles();
			}
			for (int i = 0; i < 2; i++) {
				if (mod_b[i] == index_b) {
					mod_b[i] = index_a;
				}
			}
			new_edges.add(b);

			new_edges.add(new Edge(opp_a,e.a));
			new_edges.add(new Edge(opp_b,e.b));
			}
		return new_edges;

	}
	//Find all the uncovered vertices
	public ArrayList<Vertex> find_empty_vertices() {
		ArrayList<Vertex> uncovered = new ArrayList<Vertex>();
		BufferedImage test = Low_poly.copy(image);
		Graphics2D g2d = test.createGraphics();
     	g2d.setColor(new Color(0,0,0));
        BasicStroke bs = new BasicStroke(2);
        g2d.setStroke(bs);

       	for (Triangle t: triangles) {
      		int ax = t.a.x;
      		int ay = -t.a.y + height - 1;
      		int bx = t.b.x;
      		int by = -t.b.y + height - 1;;  
      		int cx = t.c.x;
      		int cy = -t.c.y + height - 1;;  

      		int[] xpoints = {ax, bx, cx};
      		int[] ypoints = {ay, by, cy};
      		g2d.fillPolygon(xpoints, ypoints, 3);		
      	}
      	Low_poly.save(test, "Holes");
      	for (int i = 0; i < width; i++) {
      		for (int j = 0; j < height; j++) {
      			if (test.getRGB(i,j) != -16777216) {
      				uncovered.add(new Vertex(i, height - 1 - j));
      			}
      		}
      	}
		return uncovered;
	}
	//Find if three vertices are a triangle already accounted for 
	public boolean a_triangle(Vertex a, Vertex b, Vertex c) {
		for (Triangle t : triangles) {
			if (vertex_e(a,t.a) && vertex_e(b,t.b) && vertex_e(c,t.c) ||
				vertex_e(a,t.a) && vertex_e(b,t.c) && vertex_e(c,t.b) ||
				vertex_e(a,t.b) && vertex_e(b,t.a) && vertex_e(c,t.c) ||
				vertex_e(a,t.b) && vertex_e(b,t.c) && vertex_e(c,t.a) ||
				vertex_e(a,t.c) && vertex_e(b,t.a) && vertex_e(c,t.b) ||
				vertex_e(a,t.c) && vertex_e(b,t.b) && vertex_e(c,t.a) ) {
				return true;
			}
		}
		return false;
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
			Integer[] tris = edge_triangles.get(e);
			System.out.println("Edge: " + e.a.x + ", " + e.a.y + " -> " + e.b.x + ", " + e.b.y);
			print_triangle(triangles.get(tris[0]));
			if (tris[1] != null) {
				print_triangle(triangles.get(tris[1]));
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
	public void compute_center() {
		int all_x = 0;
		int all_y = 0;
		for (int i = 0; i < points.size(); i++) {
			all_x += points.get(i).x;
			all_y += points.get(i).y;
		}
		cg = new Vertex((int) all_x / points.size(), (int) all_y / points.size());
	}	
	public float sign (Vertex p1, Vertex p2, Vertex p3) {
    	return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
	}
	public boolean inTriangle (Vertex pt, Vertex v1, Vertex v2, Vertex v3) {
		boolean b1 = sign(pt, v1, v2) < 0.0f;
    	boolean b2 = sign(pt, v2, v3) < 0.0f;
    	boolean b3 = sign(pt, v3, v1) < 0.0f;

    	return ((b1 == b2) && (b2 == b3));
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
