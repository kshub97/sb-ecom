package com.ecommerce.project.security;

import com.ecommerce.project.payload.StripePaymentDTO;
import com.ecommerce.project.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class StripeServiceImpl implements StripeService {

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeApiKey;

    @PostConstruct
    private void init(){
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public PaymentIntent paymentIntent(StripePaymentDTO stripePaymentDTO) throws StripeException {
        Customer customer;
        //Retrieve customer
        CustomerSearchParams searchParams =
                CustomerSearchParams.builder()
                        .setQuery("email:'" + stripePaymentDTO.getEmail() + "'")
                        .build();
        CustomerSearchResult customers = Customer.search(searchParams);

        //If customer does not exist create new customer else fetch it
        if (customers.getData().isEmpty()){
            CustomerCreateParams customerParams =
                    CustomerCreateParams.builder()
                            .setName(stripePaymentDTO.getName())
                            .setEmail(stripePaymentDTO.getEmail())
                            .setAddress(
                                    CustomerCreateParams.Address.builder()
                                            .setLine1(stripePaymentDTO.getAddress().getBuildingName())
                                            .setCity(stripePaymentDTO.getAddress().getCity())
                                            .setLine2(stripePaymentDTO.getAddress().getStreet())
                                            .setState(stripePaymentDTO.getAddress().getState())
                                            .setPostalCode(stripePaymentDTO.getAddress().getPincode())
                                            .setCountry(stripePaymentDTO.getAddress().getCountry())
                                            .build()
                            )
                            .build();
            customer = Customer.create(customerParams);

        }else{
            //fetch customer
            customer = customers.getData().get(0); //or getFirst() ?
        }

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(stripePaymentDTO.getAmount())
                        .setCurrency(stripePaymentDTO.getCurrency())
                        .setCustomer(customer.getId())
                        .setDescription(stripePaymentDTO.getDescription())
                        .setReceiptEmail(stripePaymentDTO.getEmail())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

        //after payment intent creation return it
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return paymentIntent;
    }
}
