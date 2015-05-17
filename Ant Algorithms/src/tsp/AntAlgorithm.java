package tsp;

import java.text.CharacterIterator;

public class AntAlgorithm {

	// =====================
	// CONSTANTS
	// ======================
	public static final double ALPHA = 1.0;
	public static final double BETA = 1.0;
	public static final double RHO = 0.5;
	public static final int QVAL = 500;

	// =====================
	// VARIABLES
	// ====================
	double INIT_PHEROMONE;
	City[] cities;
	Ant[] ants;
	double[][] distance;
	double[][] pheromone;
	int bestIndex;
	int[] bestPath;
	double bestPathLength = Double.MAX_VALUE;

	/**
	 * Initialize the cities, their distances and the ant population
	 * @param nCities number of cities
	 */
	private void init(int nCities) {
		// Initialise variables
		INIT_PHEROMONE = 1.0 / nCities;
		distance = new double[nCities][nCities];
		pheromone = new double[nCities][nCities];
		bestPath = new int[nCities];
		
		createCities(nCities);
		createAnts(nCities);
	}

	private void createCities(int nCities) {
		
		int from, to;
		// Create the cities and their locations
		cities = new City[nCities];
		for (from = 0; from < nCities; from++) {
			// randomly place a city
			cities[from] = new City();
			
			for(to = 0; to < nCities; to++) {
				distance[from][to]  = 0.0;
				pheromone[from][to] = INIT_PHEROMONE;
			}
		}
		
		// compute the distances for each of the cities on the map
		for(from = 0; from < nCities; from++) {
			for(to = 0; to < nCities; to++) {
				if((to != from) && (distance[from][to] == 0.0)) {
					int xd = Math.abs(cities[from].x - cities[to].x);
					int yd = Math.abs(cities[from].y - cities[to].y);

					double d = Math.sqrt(xd * xd + yd * yd);
					distance[from][to] = d;
					distance[to][from] = d;
				}
			}
		}
		
	}

	private void createAnts(int nAnts) {
		ants = new Ant[nAnts];
		for (int i = 0; i < nAnts; i++) {
			// Distribute ants to each of the cities
			ants[i] = new Ant(cities.length, i);
		}
	}

	/**
	 * Reinitialise the ant population to start another tour around the graph
	 */
	private void restartAnts() {
		
		int ai;
		
		for(ai = 0; ai < ants.length; ai++) {
			Ant ant  = ants[ai];
			
			System.out.println("Tour Length of " + ant.tourLength + " vs best of " + bestPathLength);
			
			if(ant.tourLength < bestPathLength) {
				bestPathLength = ant.tourLength;
				bestIndex = ai;
				for(int pi = 0; pi < cities.length; pi++) // copy best path
					bestPath[pi] = ant.path[pi];
			}
			
			ant.reset(cities.length, ai);
			
		}
	}
	
	/**
	 * @param from
	 *            city index ant is currently in
	 * @param to
	 *            city index ant is moving towards
	 * @return relative probability of ant choosing this edge
	 */
	double antProduct(int from, int to) {
	
		return ((Math.pow(pheromone[from][to], ALPHA)) * (Math.pow(
				(1.0 / distance[from][to]), BETA)));
	}

	/**
	 * Using the path probability selection algorithm and the current pheromone
	 * levels of the graph, select the next city the ant will travel to
	 * 
	 * @param ant
	 *            index
	 * @return city index ant has decided to go
	 */
	int selectNextCity(Ant ant) {
		int from, to;
		
		double denom = 0.0;

		from = ant.curCity;

		// compute denom
		for (to = 0; to < cities.length; to++) {
			if (ant.tabu[to] == 0) {
				denom += antProduct(from, to);
			}
		}

		assert denom != 0.0;

		do {
			double p;
			to++;
			if (to >= cities.length)
				to = 0;
			if (ant.tabu[to] == 0) {
				p = antProduct(from, to) / denom;
				double num = Math.random();
				if (num < p)
					break;
			}
		} while (true);

		return to;
	}

	/**
	 * Simulate a single step for each ant in the population
	 * 
	 * @return zero once all ants have completed their tours
	 */
	int simulateAnts() {
		int k;
		int moving = 0;

		for (k = 0; k < ants.length; k++) {
			
			Ant ant = ants[k];
			
			// ensure this ant still has cities to visit
			if (ant.pathIndex < cities.length) {
				ant.nextCity = selectNextCity(ant);
				ant.tabu[ant.nextCity] = 1;
				ant.path[ant.pathIndex++] = ant.nextCity;
				ant.tourLength += distance[ant.curCity][ant.nextCity];

				// Handle the final case (last city to first)
				if (ant.pathIndex == cities.length) {
					ant.tourLength += distance[ant.path[cities.length - 1]][ant.path[0]];
				}

				ant.curCity = ant.nextCity;

				moving++;
			}
		}

		return moving;
	}

	/**
	 * Update the pheromone trails on each arc based on the number of ants that
	 * have traveled over it, including the evaporation of existing pheromones
	 */
	void updateTrails() {
		int from, to;
		
		// Pheromone evaporation
		for(from = 0; from < cities.length; from++) {
			for(to = 0; to < cities.length; to++) {
				if(from != to) {
					pheromone[from][to] *= (1.0 - RHO);
					if(pheromone[from][to] < 0.0) pheromone[from][to] = INIT_PHEROMONE; 
				}
			}
		}
		
		// add new pheromone to the trails
		// look at the tours of each ant
		for(int ai = 0; ai < ants.length; ai++) {
			Ant ant = ants[ai];
			// update each leg of the tour given the tour length
			for(int i = 0; i < cities.length; i++) {
				if(i < cities.length - 1) {
					from = ant.path[i];
					to = ant.path[i + 1];
				} else {
					from = ant.path[i];
					to = ant.path[0];
				}
				
				pheromone[from][to] += ((QVAL / ant.tourLength) * RHO);
				pheromone[to][from] = pheromone[from][to];
			}
		}
	}

	
	public void start(int nCities) {
		

		int MAX_TIME = 20 * nCities;
		int curTime = 0;
		init(nCities);
		
		// check ant data
		for(Ant ant : ants) System.out.println(ant.toString());
		
		while(curTime++ < MAX_TIME * 100) {
			
			if(simulateAnts() == 0) {
				updateTrails();
				
				if(curTime != MAX_TIME) restartAnts();
				System.out.println("Time is " + curTime + " " + bestPathLength);
			}
		}
		
		System.out.println("Best Tour: " + bestPathLength);
		for(int i : bestPath) System.out.print(i + " ");
	}

	public static void main(String[] args) {
		AntAlgorithm antAlgorithm = new AntAlgorithm();
		antAlgorithm.start(10);
	}
	
}

// ================================================
// INNER CLASSES
// ================================================

class Ant {
	int curCity, nextCity;
	double[] tabu;
	int[] path;
	int pathIndex;
	double tourLength;

	Ant(int nCities, int index) {
		tabu = new double[nCities];
		path = new int[nCities];
		reset(nCities, index);
	}

	public void reset(int nCities, int index) {
		
		for (int from = 0; from < nCities; from++) {
			tabu[from] = 0;
			path[from] = -1;
		}

		curCity = index % nCities;
		pathIndex = 1;
		path[0] = curCity;
		nextCity = -1;
		tourLength = 0.0;

		// load the ant's current city into taboo
		tabu[curCity] = 1;
	}
	
	public String toString() {
		return "curCity: " + curCity + " pathLength: " + pathIndex;
	}
}

class City {
	public static final int MAP_SIZE = 200;
	int x, y;

	// constructs a randomly placed city
	public City() {
		this.x = (int) (Math.random() * 200);
		this.y = (int) (Math.random() * 200);
	}

	public City(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public static double distanceTo(City c1, City c2) {
		int xDistance = Math.abs(c1.x - c2.x);
		int yDistance = Math.abs(c1.y - c2.y);
		double distance = Math.sqrt((xDistance * xDistance)
				+ (yDistance * yDistance));

		return distance;
	}

	public String toString() {
		return new String("(" + x + "," + y + ")");
	}
}
