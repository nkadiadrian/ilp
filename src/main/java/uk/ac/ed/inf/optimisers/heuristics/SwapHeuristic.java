package uk.ac.ed.inf.optimisers.heuristics;

import uk.ac.ed.inf.optimisers.Optimiser;

import java.util.Collections;
import java.util.List;

public class SwapHeuristic implements Heuristic {
    private Optimiser optimiser;

    @Override
    public List<Integer> applyHeuristic(Optimiser optimiser) {
        this.optimiser = optimiser;

        boolean better = true;
        while (better) {
            better = false;
            for (int i = 1; i < optimiser.visitOrder.size() - 1; i++) {
                if (trySwap(i)) {
                    better = true;
                }
            }
        }

        return optimiser.visitOrder;
    }

    private boolean trySwap(int i) {
        double oldCost = optimiser.getTourValue();
        int j = (i + 1);
        Collections.swap(optimiser.visitOrder, i, j);
        if (optimiser.getTourValue() < oldCost) {
            return true;
        } else {
            Collections.swap(optimiser.visitOrder, i, j);
            return false;
        }
    }
}
