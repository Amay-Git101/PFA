package events;

import models.Transaction;
import java.util.ArrayList;
import java.util.List;

public class TransactionEventManager {
    private static TransactionEventManager instance;
    private List<TransactionListener> listeners;
    
    private TransactionEventManager() {
        listeners = new ArrayList<>();
    }
    
    public static synchronized TransactionEventManager getInstance() {
        if (instance == null) {
            instance = new TransactionEventManager();
        }
        return instance;
    }
    
    public void subscribe(TransactionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void unsubscribe(TransactionListener listener) {
        listeners.remove(listener);
    }
    
    public void notifyTransactionAdded(Transaction transaction) {
        for (TransactionListener listener : listeners) {
            listener.onTransactionAdded(transaction);
        }
    }
    
    public void notifyTransactionDeleted(int transactionId) {
        for (TransactionListener listener : listeners) {
            listener.onTransactionDeleted(transactionId);
        }
    }
    
    public void notifyTransactionUpdated(Transaction transaction) {
        for (TransactionListener listener : listeners) {
            listener.onTransactionUpdated(transaction);
        }
    }
    
    public void notifyTransactionsRefreshed() {
        for (TransactionListener listener : listeners) {
            listener.onTransactionsRefreshed();
        }
    }
}
