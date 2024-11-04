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
        for (Grain g : Grain.values()){
            while (stock.get(g) < order.get(g)){
                // If g is our specialty grain, just wait for more deliveries
                if (g == specialty){
                    synchronized(this){
                            wait(); 
                    }
                } else {
                    // For another grain, try to swap if we have enough specialty grain
                    int amountNeeded = order.get(g) - stock.get(g);
                    int specialtyAvailable = stock.get(specialty);
                    
                    if (specialtyAvailable >= amountNeeded){
                        // Swap the specialty grain for the other grain with that trader
                        P3.specialist(g).swap(specialty, amountNeeded);

                        synchronized(this){
                            // Once the swap is done, deduct the amount from the specialty grain and add it to the other grain
                            stock.compute(specialty, (k, v) -> v - amountNeeded);
                            stock.compute(g, (k, v) -> v + amountNeeded);
                            //System.out.println("Trader of " + specialty + "requested " + amountNeeded + " " + g + " and got it");
                        }
                    } else {
                        // If we don't have enough specialty grain, wait for more deliveries
                        synchronized(this){
                            wait();
                        }
                    }
                }
            }
        }
        // If the order is fulfilled, deduct the amount from the stock
        deductOrder(order);
    }

    public synchronized void swap(Grain what, int amt) throws InterruptedException {
        //System.out.println("Trader of " + specialty + " received a swap request for " + amt + " " + what);
        // Keep checking until we can fulfill the swap
        while (stock.get(specialty) < amt){
            wait();
        }
        // We have enough specialty grain, so we can swap
        stock.compute(specialty, (k, v) -> v - amt);
        stock.compute(what, (k, v) -> v + amt);   
        
        //System.out.println("Stock after swap: " + stock);

        //System.out.println("Trader of " + specialty + " swapped " + amt + " " + specialty + " for " + amt + " " + what);
    }

    public synchronized void deliver(int amt) throws InterruptedException {
        //System.out.println("Delivering " + amt + " " + specialty + " to the trader");
        //System.out.println("Stock before delivery: " + stock);
        stock.compute(specialty, (k, v) -> v + amt);
        //System.out.println("Stock after delivery: " + stock);
        notifyAll();
    }

    private synchronized void deductOrder(Order order){
        for (Grain g : Grain.values()){
            stock.compute(g, (k, v) -> v - order.get(g));
        }
    }
}