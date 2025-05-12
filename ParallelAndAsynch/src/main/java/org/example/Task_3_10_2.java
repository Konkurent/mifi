package org.example;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

public class Task_3_10_2 {

    private static class BankAccountRepository {

        private final BankAccount[] bankAccounts;

        private BankAccountRepository(BankAccount[] bankAccounts) {
            this.bankAccounts = bankAccounts;
        }

        public BankAccount getBankAccountByCustomerId(int customerId) {
            try {
                TimeUnit.MICROSECONDS.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return bankAccounts[customerId];
        }

        public List<BankAccount> getAllBankAccounts() {
            try {
                TimeUnit.MICROSECONDS.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return List.of(bankAccounts);
        }
    }

    private static class BankAccountService {
        private final BankAccountRepository bankAccountRepository;

        private BankAccountService(BankAccountRepository bankAccountRepository) {
            this.bankAccountRepository = bankAccountRepository;
        }

        public boolean transfer(Transaction transaction) {
            try {
                if (bankAccountRepository.getBankAccountByCustomerId(transaction.fromId).withDraw(transaction.amount)) {
                    return bankAccountRepository.getBankAccountByCustomerId(transaction.toId).deposit(transaction.amount);
                } else return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public void report() {
            List<BankAccount> bankAccounts = bankAccountRepository.getAllBankAccounts();
            for (int i = 0; i < bankAccounts.size(); i++) {
                System.out.printf("User %s final balance: %s%n", i, bankAccounts.get(i).getBalance());
            }
        }
    }

    private static class BankAccount {
        private volatile int balance;

        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock readLock = rwLock.readLock();
        private final Lock lock = rwLock.writeLock();

        private BankAccount(int balance) {
            this.balance = balance;
        }

        public int getBalance() {
            readLock.lock();
            try {
                return balance;
            } finally {
                readLock.unlock();
            }
        }

        public boolean withDraw(int amount) {
            lock.lock();
            try {
                if (balance < amount) {
                    System.err.println("No money");
                    return false;
                }
                balance -= amount;
                return true;
            } finally {
                lock.unlock();
            }
        }

        public boolean deposit(int amount) {
            lock.lock();
            try {
                balance += amount;
                return true;
            } finally {
                lock.unlock();
            }
        }

    }

    static class Transaction {
        final int fromId;
        final int toId;
        final int amount;

        Transaction(int fromId, int toId, int amount) {
            this.fromId = fromId;
            this.toId = toId;
            this.amount = amount;
        }
    }

    static class TransactionTask extends Transaction implements Callable<Boolean> {
        private final BankAccountService bankAccountService;

        TransactionTask(BankAccountService bankAccountService, Transaction transaction) {
            super(transaction.fromId, transaction.toId, transaction.amount);
            this.bankAccountService = bankAccountService;
        }

        @Override
        public Boolean call() {
            boolean success = bankAccountService.transfer(this);
            System.out.println(Thread.currentThread().getName() + " " + fromId + " -> " + toId + " [" + amount + "] " + (success ? "success" : "fail"));
            return success;
        }
    }

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            Integer n = Integer.parseInt(scanner.nextLine());
            String[] line = scanner.nextLine().trim().split("\\s+");
            BankAccount[] balances;
            if (line.length == n) {
                balances = Arrays.stream(line).map(Integer::parseInt).map(BankAccount::new).toArray(BankAccount[]::new);
            } else {
                throw new InterruptedException("Invalid input balance array!");
            }
            int m = Integer.parseInt(scanner.nextLine());
            Transaction[] transactions = new Transaction[m];
            for (int i = 0; i < m; i++) {
                String[] input = scanner.nextLine().trim().split("\\s*-\\s*");
                transactions[i] = new Transaction(Integer.parseInt(input[0]), Integer.parseInt(input[2]), Integer.parseInt(input[1]));
            }

            BankAccountRepository repository = new BankAccountRepository(balances);
            BankAccountService service = new BankAccountService(repository);


            EXECUTOR.invokeAll(Arrays.stream(transactions).map(it -> new TransactionTask(service, it)).toList());

//            IntStream.range(0, 500000).mapToObj(it -> new TransactionTask(service, new Transaction(new Random().nextInt(balances.length), new Random().nextInt(balances.length), new Random().nextInt(500)))).forEach(EXECUTOR::submit);

            EXECUTOR.shutdown();
            if (!EXECUTOR.awaitTermination(2, TimeUnit.MINUTES)) {
                List<Runnable> unProceed = EXECUTOR.shutdownNow();
                System.out.println("Unproceed tasks: " + unProceed.size());
            }
            service.report();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.out.println("Try again");
            main(args);
        }

    }

}