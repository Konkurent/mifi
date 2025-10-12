package skillfactory;

public class Table {

    private final Fork[] forks;
    private final Philosopher[] philosophers;

    public Table(int philosophers) {
        if (philosophers < 2) {
            throw new IllegalArgumentException("Philosopher count must be greater than 2");
        }
        forks = new Fork[philosophers - 1];
        for (int i = 0; i < forks.length; i++) {
            forks[i] = new Fork();
        }
        this.philosophers = new Philosopher[philosophers];
        for (int i = 0; i < philosophers; i++) {
            Fork[] nearestForks = getForks(i);
            this.philosophers[i] = new Philosopher(i, nearestForks[0], nearestForks[1]);
        }
        for (int i = 0; i < philosophers; i++) {
            this.philosophers[i].start();
        }
    }

    private Fork[] getForks(int i) {
        if (i == 0) {
            return new Fork[] {forks[forks.length - 1], forks[i]};
        } else if (i == forks.length) {
            return new Fork[] {forks[i - 1], forks[0]};
        } else {
            return new Fork[] {forks[i - 1], forks[i]};
        }
    }

}
