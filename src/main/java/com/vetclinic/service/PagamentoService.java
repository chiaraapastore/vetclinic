package com.vetclinic.service;

import com.vetclinic.config.AuthenticationService;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final AppuntamentoRepository appuntamentoRepository;
    private final UtenteRepository utenteRepository;
    private final AuthenticationService authenticationService;
    private final FatturaRepository fatturaRepository;
    private final NotificheService notificheService;
    private final ClienteRepository clienteRepository;



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

    @Transactional
    public Pagamento processPayment(Long appointmentId, double amount, Long clienteId) {


        Cliente client = (Cliente) utenteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));

        Appuntamento appointment = appuntamentoRepository.findByIdWithCliente(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));


        if (appointment.getCliente() == null || appointment.getCliente().getId() == null) {
            throw new IllegalArgumentException("Cliente mancante nell'appuntamento");
        }

        if (!appointment.getCliente().getId().equals(clienteId)) {
            throw new IllegalArgumentException("L'appuntamento non appartiene al cliente");
        }


        Fattura invoice = new Fattura();
        invoice.setCliente(client);
        invoice.setIssueDate(new Date());
        invoice.setAmount(amount);
        invoice.setStatus("PENDING");
        fatturaRepository.save(invoice);
        fatturaRepository.flush();

        Pagamento payment = new Pagamento();
        payment.setAmount(amount);
        payment.setFattura(invoice);
        payment.setPaymentDate(new Date());
        payment.setStatus("COMPLETED");
        pagamentoRepository.save(payment);





        appointment.setStatus("PAID");
        appuntamentoRepository.save(appointment);

        notificheService.sendPaymentNotificationToClient(client, payment);
        notificheService.sendPaymentNotificationToVeterinarian(appointment.getVeterinarian(), payment);


        return payment;
    }

}
