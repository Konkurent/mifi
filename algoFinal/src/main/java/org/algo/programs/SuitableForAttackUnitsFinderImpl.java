package org.algo.programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.*;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    /**
     * Алгоритмическая сложность предоставленного кода можно оценить как O(n * m),
     * где n — это количество строк в списке `unitsByRow` (в данном случае 3),
     * а m — среднее количество элементов в каждой из строк. Каждый вызов `resolveAvailableUnits` выполняется за O(m),
     * так как он проходит по всем элементам заданной строки и выполняет `putIfAbsent`,
     * что в среднем выполняется за O(1) благодаря использованию хеш-таблицы.
     * Таким образом, сложность всего метода определяется как произведение количества строк на количество элементов в каждой строке.
     * @param unitsByRow 3 слойный массив противников
     * @param isLeftArmyTarget - кто под атакой
     * @return
     */
    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        Map<Integer, Unit> result = new HashMap<>();
        if (isLeftArmyTarget) {
            for (int i = 2; i >= 0; i--) {
                resolveAvailableUnits(result, unitsByRow.get(i));
            }
        } else {
            for (int i = 0; i <= 2; i++) {
                resolveAvailableUnits(result, unitsByRow.get(i));
            }
        }
        return new ArrayList<>(result.values());
    }

    private void resolveAvailableUnits(Map<Integer, Unit> availableUnits, List<Unit> rowUnits) {
        for (Unit rowUnit : rowUnits) {
            if (rowUnit.isAlive()) {
                availableUnits.putIfAbsent(rowUnit.getyCoordinate(), rowUnit);
            }
        }
    }

}
