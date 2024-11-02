import java.util.concurrent.ConcurrentHashMap;

public class TraderImpl implements Trader {
    private final Grain specialty;
    private final ConcurrentHashMap<Grain, Integer> stock = new ConcurrentHashMap<>();

    public TraderImpl(Grain specialty) {
        this.specialty = specialty;
        for (Grain g : Grain.values()) {
            stock.put(g, 0);
        }
    }

    public Order getAmountOnHand() {
        Order order = new Order();
        for (Grain g : Grain.values()) {
            order.set(g, stock.get(g));
        }
        return order;
    }

    public void get(Order order) throws InterruptedException {
        // Keep checking each grain type until we can fulfill the order
        for (Grain g : Grain.values()) {
            while (stock.get(g) < order.get(g)) {
                if (g == specialty) {
                    // For our specialty grain, just wait for more deliveries
                    synchronized(this) {
                        wait();
                    }
                } else {
                    // For other grains, try to swap if we have enough specialty grain
                    int amountNeeded = order.get(g) - stock.get(g);
                    int specialtyAvailable = stock.get(specialty);
                    
                    if (specialtyAvailable >= amountNeeded) {
                        // Try to atomically deduct our specialty grain
                        boolean success = stock.replace(specialty, specialtyAvailable, 
                                                     specialtyAvailable - amountNeeded);
                        
                        if (success) {
                            try {
                                P3.specialist(g).swap(specialty, amountNeeded);
                            } catch (InterruptedException e) {
                                // If swap fails, restore our specialty grain
                                stock.compute(specialty, (k, v) -> v + amountNeeded);
                                throw e;
                            }
                        }
                    } else {
                        // Not enough specialty grain to trade, wait for more
                        synchronized(this) {
                            wait();
                        }
                    }
                }
            }
            // Deduct the amount from our stock once we have enough
            stock.compute(g, (k, v) -> v - order.get(g));
        }
    }

    public void swap(Grain what, int amt) throws InterruptedException {
        // Try to perform the swap without full synchronization
        while (true) {
            int currentSpecialty = stock.get(specialty);
            if (currentSpecialty >= amt) {
                // Try to atomically deduct specialty grain
                if (stock.replace(specialty, currentSpecialty, currentSpecialty - amt)) {
                    // Add the received grain
                    stock.compute(what, (k, v) -> v + amt);
                    synchronized(this) {
                        notifyAll(); // Wake up waiting threads
                    }
                    return;
                }
            } else {
                synchronized(this) {
                    wait();
                }
            }
        }
    }

    public synchronized void deliver(int amt) throws InterruptedException {
        stock.compute(specialty, (k, v) -> v + amt);
        notifyAll();
    }
}