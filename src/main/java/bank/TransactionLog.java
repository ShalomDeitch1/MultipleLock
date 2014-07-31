package bank;

import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionLog {
	private ConcurrentSkipListMap<Long, Transaction>log = new ConcurrentSkipListMap<>();
	private AtomicLong order = new AtomicLong(0);
	
	public  void add(Transaction transaction) {
		log.put(order.getAndIncrement(), transaction);	
	}
	
	public Iterator<Transaction>iterateValues() {
		return log.values().iterator();
	}
}
