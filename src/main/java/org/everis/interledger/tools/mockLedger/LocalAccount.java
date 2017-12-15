package org.everis.interledger.tools.mockLedger;

/**
 * entity representing an account with an ILP address, a password and a balance.
 */
public class LocalAccount {
    final public String id;
    final public String password;
    private int /* TODO:(0) Move to BigInteger*/ balance;


    /**
     * Constructor for an account with an ILP address, a password and a balance.
     * @param id
     * @param password
     * @param balance
     */
    public LocalAccount(String id, String password, int balance) {
        this.id = id;
        this.password = password;
        this.balance = balance;
    }
    public LocalAccount(LocalAccount other, int newBalance) {
        this(other.id, other.password, newBalance);
    }


    public String getPassword() {
        return password;
    }

    public int getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.id).append(" | ").append(balance);
        return str.toString();
    }

    @Override
    public boolean equals(Object other) {
        boolean result =  false;
        if (other == this) return true;
        if (other == null) return false;
        if (! (other instanceof  LocalAccount)) return false;
        LocalAccount lOther = (LocalAccount)other;
        return
            lOther.id.equals(id) &&
            lOther.password.equals(password) &&
            lOther.balance == balance;
    }
}
