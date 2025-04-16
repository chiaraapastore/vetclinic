package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final AppuntamentoRepository appuntamentoRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private final FatturaRepository fatturaRepository;
    private final NotificheService notificheService;
    private final ClienteRepository clienteRepository;


    @Value("${nexi.api.url}")
    private String nexiApiUrl;

    @Value("${nexi.api.key}")
    private String nexiApiKey;

    public PagamentoService(PagamentoRepository pagamentoRepository, AppuntamentoRepository appuntamentoRepository,
                            UtenteRepository utenteRepository, AuthenticationService authenticationService,
                            FatturaRepository fatturaRepository, NotificheService notificheService,
                            ClienteRepository clienteRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.appuntamentoRepository = appuntamentoRepository;
        this.utenteRepository = utenteRepository;
        this.authenticationService = authenticationService;
        this.fatturaRepository = fatturaRepository;
        this.notificheService = notificheService;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Pagamento processPayment(Long appointmentId, double amount) {

        Utente client = utenteRepository.findByUsername(authenticationService.getUsername());
        if (client == null) {
            throw new IllegalArgumentException("Cliente non trovato");
        }

        Appuntamento appointment = appuntamentoRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));


        if (!appointment.getCliente().equals(client)) {
            throw new IllegalArgumentException("L'appuntamento non appartiene al cliente");
        }


        String paymentToken = requestNexiPayment(amount);


        Fattura invoice = new Fattura();
        invoice.setCliente((Cliente) client);
        invoice.setIssueDate(new java.util.Date());
        invoice.setAmount(amount);
        invoice.setStatus("PENDING");

        fatturaRepository.save(invoice);


        Pagamento payment = new Pagamento();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setPaymentDate(new java.util.Date());
        payment.setStatus("COMPLETED");
        payment.setTransactionId(paymentToken);

        pagamentoRepository.save(payment);


        appointment.setStatus("PAID");
        appuntamentoRepository.save(appointment);


        notificheService.sendPaymentNotificationToClient((Cliente) client, payment);
        notificheService.sendPaymentNotificationToVeterinarian(appointment.getVeterinarian(), payment);

        return payment;
    }

    private String requestNexiPayment(double amount) {

        Utente client = utenteRepository.findByUsername(authenticationService.getUsername());
        if (client == null) {
            throw new IllegalArgumentException("Cliente non trovato");
        }

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("amount", amount);
        paymentRequest.put("currency", "EUR");
        paymentRequest.put("description", "Servizio Veterinario");

        paymentRequest.put("api_key", nexiApiKey);


        String response = restTemplate.postForObject(nexiApiUrl + "/process", paymentRequest, String.class);

        return parseNexiResponse(response);
    }

    private String parseNexiResponse(String response) {
        Utente client = utenteRepository.findByUsername(authenticationService.getUsername());
        if (client == null) {
            throw new IllegalArgumentException("Cliente non trovato");
        }

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("token", "paymentToken123");
        return responseMap.get("token");
    }


    @Transactional
    public Pagamento updatePaymentStatus(Long paymentId, String status) {
        Utente client = utenteRepository.findByUsername(authenticationService.getUsername());
        if (client == null) {
            throw new IllegalArgumentException("Cliente non trovato");
        }

        Pagamento payment = pagamentoRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento non trovato"));
        payment.setStatus(status);
        pagamentoRepository.save(payment);
        return payment;
    }

}
