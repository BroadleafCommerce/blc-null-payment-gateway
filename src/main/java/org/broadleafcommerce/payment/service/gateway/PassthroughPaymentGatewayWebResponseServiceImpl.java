/*
 * #%L
 * BroadleafCommerce Framework Web
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.lang.ArrayUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.payment.PaymentAdditionalFieldType;
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayWebResponseService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponsePrintService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.passthrough.service.payment.PassthroughPaymentGatewayConstants;
import org.broadleafcommerce.vendor.passthrough.service.payment.PassthroughPaymentGatewayType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This is an example implementation of a {@link PaymentGatewayWebResponseService}.
 * This will translate the Post information back from
 * {@link org.broadleafcommerce.vendor.passthrough.web.controller.mock.processor.PassthroughMockProcessorController}
 * into a PaymentResponseDTO for processing in the Broadleaf System.
 *
 * Replace with a real Payment Gateway Integration like Braintree or PayPal PayFlow.
 *
 * In order to use load this demo service, you will need to component scan
 * the package "com.broadleafcommerce".
 *
 * This should NOT be used in production, and is meant solely for demonstration
 * purposes only.
 *
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPassthroughPaymentGatewayWebResponseService")
public class PassthroughPaymentGatewayWebResponseServiceImpl extends AbstractPaymentGatewayWebResponseService {

    @Resource(name = "blPaymentGatewayWebResponsePrintService")
    protected PaymentGatewayWebResponsePrintService webResponsePrintService;

    @Resource(name = "blPassthroughPaymentGatewayConfiguration")
    protected PassthroughPaymentGatewayConfiguration configuration;

    @Override
    public PaymentResponseDTO translateWebResponse(HttpServletRequest request) throws PaymentException {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.CREDIT_CARD,
                PassthroughPaymentGatewayType.NULL_GATEWAY)
                .rawResponse(webResponsePrintService.printRequest(request));

        Map<String,String[]> paramMap = request.getParameterMap();

        Money amount = Money.ZERO;
        if (paramMap.containsKey(PassthroughPaymentGatewayConstants.TRANSACTION_AMT)) {
            String amt = paramMap.get(PassthroughPaymentGatewayConstants.TRANSACTION_AMT)[0];
            amount = new Money(amt);
        }

        boolean approved = false;
        if (paramMap.containsKey(PassthroughPaymentGatewayConstants.RESULT_SUCCESS)) {
            String[] msg = paramMap.get(PassthroughPaymentGatewayConstants.RESULT_SUCCESS);
            if (ArrayUtils.contains(msg, "true")) {
                approved = true;
            }
        }

        PaymentTransactionType type = PaymentTransactionType.AUTHORIZE_AND_CAPTURE;
        if (!configuration.isPerformAuthorizeAndCapture()) {
            type = PaymentTransactionType.AUTHORIZE;
        }

        responseDTO.successful(approved)
                .amount(amount)
                .paymentTransactionType(type)
                .orderId(parse(paramMap, PassthroughPaymentGatewayConstants.ORDER_ID))
                .customer()
                    .customerId(parse(paramMap, PassthroughPaymentGatewayConstants.CUSTOMER_ID))
                    .done()
                .paymentToken(parse(paramMap, PassthroughPaymentGatewayConstants.PAYMENT_TOKEN_ID))
                .responseMap(PassthroughPaymentGatewayConstants.GATEWAY_TRANSACTION_ID,
                        parse(paramMap, PassthroughPaymentGatewayConstants.GATEWAY_TRANSACTION_ID))
                .responseMap(PassthroughPaymentGatewayConstants.RESULT_MESSAGE,
                        parse(paramMap, PassthroughPaymentGatewayConstants.RESULT_MESSAGE))
                .responseMap(PaymentAdditionalFieldType.TOKEN.getType(),
                        parse(paramMap, PassthroughPaymentGatewayConstants.PAYMENT_TOKEN_ID))
                .responseMap(PaymentAdditionalFieldType.LAST_FOUR.getType(),
                        parse(paramMap, PassthroughPaymentGatewayConstants.CREDIT_CARD_LAST_FOUR))
                .responseMap(PaymentAdditionalFieldType.CARD_TYPE.getType(),
                        parse(paramMap, PassthroughPaymentGatewayConstants.CREDIT_CARD_TYPE))
                .responseMap(PaymentAdditionalFieldType.NAME_ON_CARD.getType(),
                        parse(paramMap, PassthroughPaymentGatewayConstants.CREDIT_CARD_NAME))
                .responseMap(PaymentAdditionalFieldType.EXP_DATE.getType(),
                        parse(paramMap, PassthroughPaymentGatewayConstants.CREDIT_CARD_EXP_DATE))
                .billTo()
                    .addressFirstName(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_FIRST_NAME))
                    .addressLastName(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_LAST_NAME))
                    .addressLine1(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_ADDRESS_LINE1))
                    .addressLine2(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_ADDRESS_LINE2))
                    .addressCityLocality(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_CITY))
                    .addressStateRegion(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_STATE))
                    .addressPostalCode(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_ZIP))
                    .addressCountryCode(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_COUNTRY))
                    .addressPhone(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_PHONE))
                    .addressEmail(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_EMAIL))
                    .addressCompanyName(parse(paramMap, PassthroughPaymentGatewayConstants.BILLING_COMPANY_NAME))
                    .done()
                .creditCard()
                    .creditCardHolderName(parse(paramMap, PassthroughPaymentGatewayConstants.CREDIT_CARD_NAME))
                    .creditCardLastFour(parse(paramMap, PassthroughPaymentGatewayConstants.CREDIT_CARD_LAST_FOUR))
                    .creditCardType(parse(paramMap, PassthroughPaymentGatewayConstants.CREDIT_CARD_TYPE))
                    .creditCardExpDate(parse(paramMap, PassthroughPaymentGatewayConstants.CREDIT_CARD_EXP_DATE))
                    .done();

        return responseDTO;

    }

    protected String parse(Map<String,String[]> paramMap, String param) {
        if (paramMap.containsKey(param)) {
            return paramMap.get(param)[0];
        }

        return null;
    }


}
