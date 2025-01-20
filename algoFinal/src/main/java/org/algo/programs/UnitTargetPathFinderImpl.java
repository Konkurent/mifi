package org.algo.programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;
    private static final int[][] DIRECTIONS = new int[][]{
            {1, 0},
            {1, 1},
            {1, -1},
            {0, -1},
            {0, 1},
            {-1, 0},
            {-1, -1},
            {-1, 1},
    };

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        final ToDoubleFunction<Edge> EDGE_DISTANCE = e -> Math.sqrt(Math.pow((double) targetUnit.getxCoordinate() - e.getX(), 2) + Math.pow((double) targetUnit.getyCoordinate() - e.getY(), 2));
        boolean[][] occupied = new boolean[WIDTH][HEIGHT];
        existingUnitList.forEach(existingUnit -> occupied[existingUnit.getxCoordinate()][existingUnit.getyCoordinate()] = existingUnit.isAlive());
        Queue<Edge> queue = new PriorityQueue<>(Comparator.comparingDouble(EDGE_DISTANCE));
        Map<String, Edge> cameFrom = new HashMap<>();
        queue.add(new Edge(attackUnit.getxCoordinate(), attackUnit.getyCoordinate()));
        occupied[attackUnit.getxCoordinate()][attackUnit.getyCoordinate()] = true;

        while (!queue.isEmpty()) {
            Edge current = queue.poll();
            if (current.getX() == targetUnit.getxCoordinate() && current.getY() == targetUnit.getyCoordinate()) {
                return reconstructPath(cameFrom, targetUnit);
            }
            for (int[] dir : DIRECTIONS) {
                int newX = current.getX() + dir[0];
                int newY = current.getY() + dir[1];
                if (newY == targetUnit.getyCoordinate() && newX == targetUnit.getxCoordinate()){
                    System.out.println();
                }
                if ((isValid(newX, newY) && !occupied[newX][newY]) || (newX == targetUnit.getxCoordinate() && newY == targetUnit.getyCoordinate())) {
                    if (!occupied[newX][newY] || (newX == targetUnit.getxCoordinate() && newY == targetUnit.getyCoordinate())) {
                        String newPos = newX + "," + newY;
                        queue.add(new Edge(newX, newY));
                        occupied[newX][newY] = true;
                        cameFrom.put(newPos, current);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    private List<Edge> reconstructPath(Map<String, Edge> cameFrom, Unit target) {
        List<Edge> path = new ArrayList<>();
        Edge step = new Edge(target.getxCoordinate(), target.getyCoordinate());

        while (step != null) {
            path.add(new Edge(step.getX(), step.getY()));
            step = cameFrom.get(step.getX() + "," + step.getY());
        }

        Collections.reverse(path);
        return path;
    }
}
