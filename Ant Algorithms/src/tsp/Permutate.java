package tsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Permutate {
	
	float distance[][];
	
	
	public double solveTSP(float distance[][]) {
		this.distance = distance;
		List<Integer> cities = new ArrayList<>();
		for(int i = 0; i < distance.length; i++) cities.add(i);
		
		double bestTour = Double.MAX_VALUE;
		permute(cities, 0, bestTour);
		
		return bestTour;
	}

	private void permute(List<Integer> arr, int k, double bestTour) {
		for(int i = k;  i < arr.size(); i++) {
			Collections.swap(arr, i , k);
			permute(arr, k + 1, bestTour);
			Collections.swap(arr, k, i);
		}
		if (k == arr.size() -1){
            //System.out.println(java.util.Arrays.toString(arr.toArray()));
			double tourLength = 0;
			for(int i = 0; i < arr.size(); i++) {
				int to, from;
				to = (i == arr.size() - 1) ? 0 : i + 1; 
				from = i;
				tourLength += distance[to][from];
				bestTour = Math.min(tourLength, bestTour);
			}
        }
	}
	
	public static void main(String[] args) {
		ACO aco = new ACO();
		aco.initACO();
		Permutate perm = new Permutate();
		System.out.println(perm.solveTSP(aco.precomp_distance));
	}
}
