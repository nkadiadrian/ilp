package uk.ac.ed.inf.optimisers.heuristics;

import uk.ac.ed.inf.optimisers.Optimiser;

import java.util.List;

public interface Heuristic {
    List<Integer> applyHeuristic(Optimiser optimiser);
}
