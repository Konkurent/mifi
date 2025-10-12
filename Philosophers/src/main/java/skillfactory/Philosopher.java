package skillfactory;

public class Philosopher extends Thread {

    private final Fork leftFork;
    private final Fork rightFork;

    public Philosopher(int num, Fork leftFork, Fork rightFork) {
        super("Philosopher-" + num);
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    @Override
    public void run() {
        while (true) {
            boolean left = leftFork.get();
            if (left) {
                System.out.println(Thread.currentThread().getName() + " has left fork");
            }
            boolean right = rightFork.get();
            if (right) {
                System.out.println(Thread.currentThread().getName() + " has right fork");
            }
            try {
                if (left && right) {
                    System.out.println(Thread.currentThread().getName() + " is starting eat");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(Thread.currentThread().getName() + " has stopped eat");
                } else {
                    if (left && leftFork.locked) {
                        leftFork.put();
                    }
                    if (right && rightFork.locked) {
                        rightFork.put();
                    }
                }
            } finally {
                if (left && leftFork.locked) {
                    leftFork.put();
                }
                if (right && rightFork.locked) {
                    rightFork.put();
                }
                System.out.println(Thread.currentThread().getName() + " thinking");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
