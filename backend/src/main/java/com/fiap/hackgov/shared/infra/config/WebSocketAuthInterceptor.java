package com.fiap.hackgov.shared.infra.config;

import com.fiap.hackgov.shared.infra.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor
        implements ChannelInterceptor {

    private final TokenService tokenService;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        /*
         * CONNECT
         */
        if (
                StompCommand.CONNECT.equals(
                        accessor.getCommand()
                )
        ) {

            System.out.println("CONNECT DETECTADO");

            /*
             * HEADER
             */
            String authHeader =
                    accessor.getFirstNativeHeader(
                            "authorization"
                    );

            System.out.println("HEADER:");

            System.out.println(authHeader);

            if (
                    authHeader != null &&
                            authHeader.startsWith("Bearer ")
            ) {

                try {

                    String token =
                            authHeader.substring(7);

                    System.out.println("TOKEN:");

                    System.out.println(token);

                    String email =
                            tokenService.getSubject(token);

                    System.out.println("EMAIL:");

                    System.out.println(email);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    List.of()
                            );

                    accessor.setUser(auth);

                    System.out.println(
                            "USUÁRIO AUTENTICADO"
                    );

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }

        return message;
    }
}