import java.util.Random;

class Account {
    // Поле для хранения баланса
    private double balance;

    // Объект для синхронизации потоков
    private final Object locker = new Object();

    // Конструктор для инициализации начального баланса
    public Account(double initialBalance) {
        this.balance = initialBalance;
    }

    // Метод для пополнения баланса
    public void deposit(double amount) {
        synchronized (locker) {
            balance += amount;
            System.out.println("Баланс пополнен на " + amount + ". Текущий баланс: " + balance);
            locker.notifyAll();  // Оповещение ожидающих потоков
        }
    }

    // Метод для снятия денег
    public void withdraw(double amount) {
        synchronized (locker) {
            // Ожидание, пока баланс не станет достаточным
            while (balance < amount) {
                try {
                    System.out.println("Ожидание пополнения до " + amount + "...");
                    locker.wait(); // Ожидание
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            balance -= amount;
            System.out.println("Снятие " + amount + ". Остаток на балансе: " + balance);
        }
    }

    // Метод для получения текущего баланса
    public double getBalance() {
        synchronized (locker) {
            return balance;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Account account = new Account(0); // Создаем аккаунт с начальным балансом 0

        // Поток для многократного пополнения баланса
        Thread depositThread = new Thread(() -> {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                double amount = random.nextInt(100) + 1; // Случайное число для пополнения
                account.deposit(amount);
                try {
                    Thread.sleep(random.nextInt(500)); // Задержка для симуляции реального времени
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Поток для ожидания накопления и снятия денег
        Thread withdrawThread = new Thread(() -> {
            account.withdraw(300); // Ожидание накопления 300 и снятие
        });

        // Запуск потоков
        depositThread.start();
        withdrawThread.start();

        // Ожидание завершения потоков
        try {
            depositThread.join();
            withdrawThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Вывод финального баланса
        System.out.println("Финальный баланс: " + account.getBalance());
    }
}
