package tsp;

import std.StdDraw;

public class MainActivity {
	
	AntAlgorithm antAlgorithm;
	
	private final int windowDim = 250;
	

	public MainActivity() {
		StdDraw.setScale(0, windowDim);
		antAlgorithm = new AntAlgorithm();
		antAlgorithm.start(30);
		
	}
	
	public void draw() {
		// draw cities
		for(int i = 0; i < antAlgorithm.cities.length; i++) {
			StdDraw.setPenColor(StdDraw.BLACK);
			StdDraw.setPenRadius(0.02);
			
			double x = antAlgorithm.cities[i].x;
			double y = antAlgorithm.cities[i].y;
			StdDraw.point(x, y);
			
		}
	}
	
	public void update() {
		// for now draw the shortest path
		int[] path = antAlgorithm.bestPath;
		City[] cities = antAlgorithm.cities;
		
		for(int i = 0; i < path.length; i++) {
			double x1, x2, y1, y2;
			if(i == path.length - 1) {
				x1 = cities[i].x;
				y1 = cities[i].y;
				x2 = cities[0].x;
				y2 = cities[0].y;
			} else {
				x1 = cities[i].x;
				y1 = cities[i].y;
				x2 = cities[i+1].x;
				y2 = cities[i+1].y;
			}
			
			StdDraw.setPenRadius(0.005);
			StdDraw.line(x1, y1, x2, y2);
		}
	}
	
	public static void main(String[] args) {
		MainActivity ma = new MainActivity();
		ma.draw();
		ma.update();
	}
}
