package br.com.lab.impacta.investment.domain.service.impl;

import br.com.lab.impacta.investment.domain.exception.InvestmentAccountIsNotDebitException;
import br.com.lab.impacta.investment.domain.exception.InvestmentAccountWithoutBalanceException;
import br.com.lab.impacta.investment.domain.exception.InvestmentAccountWithoutBalanceForPrivateProductException;
import br.com.lab.impacta.investment.domain.exception.InvestmentProductNotFoundException;
import br.com.lab.impacta.investment.domain.model.Investment;
import br.com.lab.impacta.investment.domain.model.Product;
import br.com.lab.impacta.investment.domain.service.InvestmentService;
import br.com.lab.impacta.investment.domain.service.facade.AccountFacade;
import br.com.lab.impacta.investment.domain.service.facade.valueObject.AccountBalanceVO;
import br.com.lab.impacta.investment.infrastructure.repository.InvestmentRepository;
import br.com.lab.impacta.investment.infrastructure.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountFacade accountFacade;

    @Value("${lab.investment.exceptions.product-dont-exists-message}")
    private String messageExceptionProductNotFound;

    @Value("${lab.investment.exceptions.product-dont-exists-description}")
    private String descriptionExceptionProductNotFound;

    @Value("${lab.investment.exceptions.account-without-balance-message}")
    private String messageExceptionAccountWithoutBalance;

    @Value("${lab.investment.exceptions.account-without-balance-description}")
    private String descriptionExceptionAccountWithoutBalance;

    @Value("${lab.investment.exceptions.account-without-balance-for-private-product-message}")
    private String messageExceptionAccountWithoutBalanceForPrivateProduct;

    @Value("${lab.investment.exceptions.account-without-balance-for-private-product-description}")
    private String descriptionExceptionAccountWithoutBalanceForPrivateProduct;

    @Value("${lab.investment.exceptions.account-is-not-debited-message}")
    private String messageExceptionAccountIfNotDebited;

    @Value("${lab.investment.exceptions.account-is-not-debited-description}")
    private String descriptionExceptionAccountIfNotDebited;

    @Override
    public Investment invest(Long productId, Long accountId, Double investmentValue) {
        Optional<Product> product = productRepository.findById(productId);

        if (product.isEmpty())
            throw new InvestmentProductNotFoundException(
                    messageExceptionProductNotFound,
                    descriptionExceptionProductNotFound);

        Investment investment = new Investment(productId, accountId, investmentValue);

        AccountBalanceVO accountBalanceVO = accountFacade.getAccountBalanceById(accountId);

        if (!investment.sufficientBalanceForInvestment(accountBalanceVO.getBalance()))
            throw new InvestmentAccountWithoutBalanceException(
                    messageExceptionAccountWithoutBalance,
                    descriptionExceptionAccountWithoutBalance);

        if (!investment.verifyPrivateOrDefaultProductForInvestment(accountBalanceVO.getBalance(),
                product.get()))
            throw new InvestmentAccountWithoutBalanceForPrivateProductException(
                    messageExceptionAccountWithoutBalanceForPrivateProduct,
                    descriptionExceptionAccountWithoutBalanceForPrivateProduct);

        boolean isDebited = accountFacade.debitAccount(accountId, investmentValue);

        if (!isDebited)
            throw new InvestmentAccountIsNotDebitException(
                    messageExceptionAccountIfNotDebited,
                    descriptionExceptionAccountIfNotDebited);

        investmentRepository.save(investment);

        return investment;
    }
}
