package events;

import models.Transaction;

public interface TransactionListener {
    void onTransactionAdded(Transaction transaction);
    void onTransactionDeleted(int transactionId);
    void onTransactionUpdated(Transaction transaction);
    void onTransactionsRefreshed();
}
