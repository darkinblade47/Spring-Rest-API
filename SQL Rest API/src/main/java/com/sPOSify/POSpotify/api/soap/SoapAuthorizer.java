package com.sPOSify.POSpotify.api.soap;

import org.springframework.stereotype.Component;

import com.sPOSify.POSpotify.errorHandling.customExceptions.UnauthorizedException;

import javax.xml.bind.JAXBException;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class SoapAuthorizer {
    private static final SoapConfig soapConfig = new SoapConfig();

    public static String[] SoapAuthorize(Optional<String> token) {
        String authorizationString;
        if (token.isPresent() && !token.get().endsWith("Bearer ")) // startsWith nu endsWith
            try {
                System.out.println(token.get());
                authorizationString = soapConfig.soapClient(soapConfig.marshaller()).AuthorizeUser(token.get().split("\\s+")[1]);
                if (authorizationString.startsWith("Error:Signature has expired"))
                    throw new UnauthorizedException("Session expired.Please log in again.");
                else if (authorizationString.startsWith("Error"))
                    throw new UnauthorizedException("Something occurred.Please log in again.");
                System.out.println(authorizationString);
            } catch (JAXBException e) {
                throw new UnauthorizedException("Something occurred.Please log in again.");
            }
        else throw new UnauthorizedException("You are not authorized.Please log in.");
        Pattern pattern = Pattern.compile("\\|\\|\\|");
        return pattern.split(authorizationString);
    }
}
