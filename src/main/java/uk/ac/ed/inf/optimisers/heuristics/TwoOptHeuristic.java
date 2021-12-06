package uk.ac.ed.inf.optimisers.heuristics;

import uk.ac.ed.inf.optimisers.Optimiser;

import java.util.Collections;
import java.util.List;

public class TwoOptHeuristic implements Heuristic {
    private Optimiser optimiser;

    @Override
    public List<Integer> applyHeuristic(Optimiser optimiser) {
        this.optimiser = optimiser;

        boolean better = true;
        while (better) {
            better = false;
            for (int j = 1; j < optimiser.visitOrder.size(); j++) {
                for (int i = 1; i < j; i++) {
                    if (tryReverse(i, j)) {
                        better = true;
                    }
                }
            }
        }

        return optimiser.visitOrder;
    }

    private boolean tryReverse(int i, int j) {
        double oldCost = optimiser.getTourValue();
        Collections.reverse(optimiser.visitOrder.subList(i, j));
        if (optimiser.getTourValue() < oldCost) {
            return true;
        } else {
            Collections.reverse(optimiser.visitOrder.subList(i, j));
            return false;
        }
    }
}
