package org.algo.programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;
import java.util.stream.Collectors;

public class GeneratePresetImpl implements GeneratePreset {

    private static final int WIDTH = 3;
    private static final int HEIGHT = 21;

    private static class UnitExt extends Unit implements Comparable<UnitExt> {

        public UnitExt(Unit unit) {
            super(
                    unit.getName(),
                    unit.getUnitType(),
                    unit.getHealth(),
                    unit.getBaseAttack(),
                    unit.getCost(),
                    unit.getAttackType(),
                    unit.getAttackBonuses(),
                    unit.getDefenceBonuses(),
                    unit.getxCoordinate(),
                    unit.getyCoordinate()
            );
        }

        public Double getAttackEfficiency() {
            return ((double) getBaseAttack()) / getCost();
        }

        public Double getHealthEfficiency() {
            return ((double) getHealth()) / getCost();
        }

        public final static UnitExt wrap(Unit unit) {
            return new UnitExt(unit);
        }

        @Override
        public int compareTo(UnitExt o) {
            Double firstSumEff = getAttackEfficiency() / getHealthEfficiency();
            Double secondSumEff = o.getAttackEfficiency() / o.getHealthEfficiency();
            return firstSumEff.compareTo(secondSumEff);
        }

        protected Unit clone(String name, int xCoordinate, int yCoordinate) {
            System.out.println("Создан юнит " + name + " в координатах (" + xCoordinate + ", " + yCoordinate + ")");
            return new Unit(
                    name,
                    getUnitType(),
                    getHealth(),
                    getBaseAttack(),
                    getCost(),
                    getAttackType(),
                    getAttackBonuses(),
                    getDefenceBonuses(),
                    xCoordinate,
                    yCoordinate
            );
        }
    }

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        List<Unit> army = new ArrayList<>();
        boolean[][] occupied = new boolean[WIDTH][HEIGHT];
        Random random = new Random();
        int balance = maxPoints;
        List<UnitExt> weighedUnits = unitList.stream().map(UnitExt::wrap).sorted(Comparator.reverseOrder()).toList();
        Map<String, Integer> unitStats = weighedUnits.stream().collect(Collectors.toMap(Unit::getUnitType, u -> 0));
        int currentUnitIndex = 0;
        while (currentUnitIndex < weighedUnits.size()) {
            UnitExt unit = weighedUnits.get(currentUnitIndex);
            if (unit.getCost() <= balance && unitStats.get(unit.getUnitType()) < 11) {
                int xCoordinate;
                int yCoordinate;
                while (true) {
                    xCoordinate = random.nextInt(WIDTH);
                    yCoordinate = random.nextInt(HEIGHT);
                    if (!occupied[xCoordinate][yCoordinate]) {
                        occupied[xCoordinate][yCoordinate] = true;
                        break;
                    }
                }
                army.add(unit.clone(unit.getUnitType() + " " + (army.size() + 1), xCoordinate, yCoordinate));
                balance -= unit.getCost();
                unitStats.put(unit.getName(), unitStats.get(unit.getUnitType()) + 1);
            } else {
                currentUnitIndex++;
            }
        }
        return new Army(army);
    }

}