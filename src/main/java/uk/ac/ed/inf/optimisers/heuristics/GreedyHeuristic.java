package uk.ac.ed.inf.optimisers.heuristics;

import uk.ac.ed.inf.optimisers.Optimiser;

import java.util.ArrayList;
import java.util.List;

public class GreedyHeuristic implements Heuristic {

    @Override
    public List<Integer> applyHeuristic(Optimiser optimiser) {
        List<Integer> visitedIndices = new ArrayList<>();
        int currentindex = 0;
        visitedIndices.add(currentindex);

        while (visitedIndices.size() < optimiser.distanceMatrix.size()) {
            List<Double> distanceRow = optimiser.distanceMatrix.get(currentindex);
            double minDist = Double.POSITIVE_INFINITY;
            int nextMove = 0;

            for (int i = 1; i < distanceRow.size(); i++) {
                if (distanceRow.get(i) < minDist & !visitedIndices.contains(i) & i != currentindex) {
                    minDist = distanceRow.get(i);
                    nextMove = i;
                }
            }
            visitedIndices.add(nextMove);
            currentindex = nextMove;
        }

        return visitedIndices;
    }
}
