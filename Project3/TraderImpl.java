import java.util.HashMap;

public class TraderImpl implements Trader {
    private final Grain specialty;
    private final HashMap<Grain, Integer> stock = new HashMap<>();

    public TraderImpl(Grain specialty) {
        this.specialty = specialty;
        for (Grain g : Grain.values()) {
            stock.put(g, 0);
        }
    }

    public synchronized Order getAmountOnHand() {
        Order order = new Order();
        for (Grain g : Grain.values()) {
            order.set(g, stock.get(g));
        }
        return order;
    }

    public void get(Order order) throws InterruptedException {
        // Loop through each grain type to fulfill the order
        for (Grain g : Grain.values()) {
            while (true) {
                synchronized (this) {
                    if (stock.get(g) >= order.get(g)) break;
                }
                
                // If grain is our specialty, wait for delivery
                if (g == specialty) {
                    synchronized (this) {
                        wait();
                    }
                } else {
                    int amountNeeded;
                    int specialtyAvailable;

                    synchronized (this) {
                        amountNeeded = order.get(g) - stock.get(g);
                        specialtyAvailable = stock.get(specialty);
                    }

                    if (specialtyAvailable >= amountNeeded) {
                        P3.specialist(g).swap(specialty, amountNeeded);

                        synchronized (this) {
                            stock.put(specialty, stock.get(specialty) - amountNeeded);
                            stock.put(g, stock.get(g) + amountNeeded);
                        }
                    } else {
                        synchronized (this) {
                            wait();
                        }
                    }
                }
            }
        }

        // Deduct the amount from the stock once the order is fulfilled
        synchronized (this) {
            deductOrder(order);
        }
    }

    public synchronized void swap(Grain what, int amt) throws InterruptedException {
        // Wait until we have enough specialty grain to fulfill the swap
        while (stock.get(specialty) < amt) {
            wait();
        }

        // Perform the swap
        stock.put(specialty, stock.get(specialty) - amt);
        stock.put(what, stock.get(what) + amt);

        // Notify any waiting threads
        notifyAll();
    }

    public synchronized void deliver(int amt) {
        // Add delivered amount to the stock
        stock.put(specialty, stock.get(specialty) + amt);
        notifyAll();
    }

    private synchronized void deductOrder(Order order) {
        for (Grain g : Grain.values()) {
            stock.put(g, stock.get(g) - order.get(g));
        }
    }
}
