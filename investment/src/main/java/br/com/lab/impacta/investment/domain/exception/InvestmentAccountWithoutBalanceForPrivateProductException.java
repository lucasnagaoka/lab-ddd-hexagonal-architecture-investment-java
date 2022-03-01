package br.com.lab.impacta.investment.domain.exception;

public class InvestmentAccountWithoutBalanceForPrivateProductException extends RuntimeException {

    private String description;

    public String getDescription() {
        return this.description;
    }

    public InvestmentAccountWithoutBalanceForPrivateProductException(){
        super();
    }

    public InvestmentAccountWithoutBalanceForPrivateProductException(String message, String description) {
        super(message);

        this.description = description;
    }
}
