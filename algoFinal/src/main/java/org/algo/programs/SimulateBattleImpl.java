package org.algo.programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog; // Позволяет логировать. Использовать после каждой атаки юнита

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        Queue<Unit> playerUnits = new PriorityQueue<>(Comparator.comparingInt(Unit::getBaseAttack).reversed());
        Queue<Unit> computerUnits = new PriorityQueue<>(Comparator.comparingInt(Unit::getBaseAttack).reversed());
        playerUnits.addAll(playerArmy.getUnits());
        computerUnits.addAll(computerArmy.getUnits());
        boolean player = new Random().nextBoolean();
        while (!playerUnits.isEmpty() && !computerUnits.isEmpty()) {
            if (player) {
                Unit target = fight(playerUnits);
                if (target == null) return;
                player = false;
            } else {
                Unit target = fight(computerUnits);
                if (target == null) return;
                player = true;
            }
        }
    }

    private Unit fight(Queue<Unit> attackerQueue) throws InterruptedException {
        Unit attacker = attackerQueue.poll();
        if (attacker != null && attacker.isAlive()) {
            Unit target = attack(attacker);
            attackerQueue.offer(attacker);
            return target;
        }
        return attacker;
    }

    private Unit attack(Unit unit) throws InterruptedException {
        Unit enemy = unit.getProgram().attack();
        printBattleLog.printBattleLog(unit, enemy);
        return enemy;
    }
}