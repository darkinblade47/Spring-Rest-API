package com.MONGOtify.LeMongo.api.soap;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import wsdl.Authorize;
import wsdl.AuthorizeResponse;
import wsdl.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class SoapClient extends WebServiceGatewaySupport {
    public String AuthorizeUser(String token) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Authorize.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        Authorize request = new Authorize();

        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> jaxbElement = objectFactory.createAuthorizeJwt(token);
        request.setJwt(jaxbElement);
        JAXBElement<Authorize> auth = objectFactory.createAuthorize(request);

        JAXBElement<AuthorizeResponse> authReponse = (JAXBElement<AuthorizeResponse>) getWebServiceTemplate().
                marshalSendAndReceive("http://127.0.0.1:8000", auth, null);

        return authReponse.getValue().getAuthorizeResult().getValue();
    }
}