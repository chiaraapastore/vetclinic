package com.vetclinic.service;

import com.itextpdf.text.pdf.draw.LineSeparator;
import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import com.vetclinic.config.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.*;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class AnimaleService {

    private final AdminService adminService;
    private final AnimaleRepository animaleRepository;
    private final ClienteRepository clienteRepository;
    private final FatturaRepository fatturaRepository;
    private final AuthenticationService authenticationService;
    private final TrattamentoRepository trattamentoRepository;
    private final SomministrazioneRepository somministrazioneRepository;
    private final KeycloakService keycloakService;
    private final NotificheService notificheService;
    private final RepartoRepository repartoRepository;
    private final VeterinarioRepository veterinarioRepository;

    public AnimaleService(AdminService adminService, VeterinarioRepository veterinarioRepository,RepartoRepository repartoRepository,NotificheService notificheService,KeycloakService keycloakService,TrattamentoRepository trattamentoRepository, SomministrazioneRepository somministrazioneRepository, AnimaleRepository animaleRepository, ClienteRepository clienteRepository, FatturaRepository fatturaRepository,AuthenticationService authenticationService) {
        this.animaleRepository = animaleRepository;
        this.clienteRepository = clienteRepository;
        this.authenticationService = authenticationService;
        this.fatturaRepository = fatturaRepository;
        this.adminService = adminService;
        this.trattamentoRepository = trattamentoRepository;
        this.somministrazioneRepository = somministrazioneRepository;
        this.keycloakService = keycloakService;
        this.notificheService = notificheService;
        this.repartoRepository = repartoRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @Transactional
    public List<Animale> getAnimalsOfClient() {
        String username = authenticationService.getUsername();
        Cliente cliente = clienteRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));
        return animaleRepository.findByCliente(cliente);
    }

    @Transactional
    public Animale getAnimaleById(Long animaleId) {
        return animaleRepository.findById(animaleId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));
    }

    @Transactional
    public Animale addAnimale(Animale animale) {
        Cliente cliente = clienteRepository.findByUsername(authenticationService.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));
        animale.setCliente(cliente);
        return animaleRepository.save(animale);
    }

    @Transactional
    public Animale updateAnimale(Long animaleId, Animale animaleDetails) {
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));
        animale.setName(animaleDetails.getName());
        animale.setSpecies(animaleDetails.getSpecies());
        animale.setBreed(animaleDetails.getBreed());
        animale.setAge(animaleDetails.getAge());
        animale.setState(animaleDetails.getState());
        animale.setMicrochip(animaleDetails.getMicrochip());
        animale.setWeight(animaleDetails.getWeight());
        return animaleRepository.save(animale);
    }

    @Transactional
    public void deleteAnimale(Long animaleId) {
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new IllegalArgumentException("Animale non trovato"));
        animaleRepository.delete(animale);
    }

    @Transactional
    public byte[] generateDocumentoClinicoAndFatturaPdf(Long animaleId) throws DocumentException {
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new RuntimeException("Animale non trovato"));

        Cliente cliente = animale.getCliente();
        Fattura fattura = fatturaRepository.findFirstByCliente(cliente)
                .orElseThrow(() -> new RuntimeException("Fattura non trovata per il cliente"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 100);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);


        try {
            Image logo = Image.getInstance("https://i.pinimg.com/736x/f4/03/61/f40361e106a2512fa85f6fb59ba3f960.jpg");
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(0, 121, 107));
        Paragraph title = new Paragraph("Documentazione Clinica e Fattura", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(0, 121, 107));
        addTableRow(table, "Nome Cliente:", cliente.getFirstName(), headerFont, textFont);
        addTableRow(table, "Cognome Cliente:", cliente.getLastName(), headerFont, textFont);
        addTableRow(table, "Email Cliente:", cliente.getEmail(), headerFont, textFont);
        document.add(table);

        Paragraph animalTitle = new Paragraph("Dati dell'Animale", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(0, 121, 107)));
        animalTitle.setAlignment(Element.ALIGN_LEFT);
        document.add(animalTitle);

        PdfPTable animalTable = new PdfPTable(2);
        animalTable.setWidthPercentage(100);
        addTableRow(animalTable, "Nome Animale:", animale.getName(), headerFont, textFont);
        addTableRow(animalTable, "Specie:", animale.getSpecies(), headerFont, textFont);
        addTableRow(animalTable, "Razza:", animale.getBreed(), headerFont, textFont);
        addTableRow(animalTable, "Età:", String.valueOf(animale.getAge()), headerFont, textFont);
        addTableRow(animalTable, "Microchip:", String.valueOf(animale.getMicrochip()), headerFont, textFont);
        addTableRow(animalTable, "Peso:", String.valueOf(animale.getWeight()), headerFont, textFont);
        addTableRow(animalTable, "Diagnosi:", animale.getState(), headerFont, textFont);
        document.add(animalTable);


        document.close();
        return baos.toByteArray();
    }

    @Transactional
    public byte[] generateCartellaClinicaPdf(Long animaleId) throws DocumentException {
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new RuntimeException("Animale non trovato"));

        Cliente cliente = animale.getCliente();
        if (cliente == null) {
            throw new RuntimeException("Cliente associato non trovato");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 70);
        PdfWriter.getInstance(document, baos);
        document.open();

        try {
            Image logo = Image.getInstance("https://i.pinimg.com/736x/7f/a4/c7/7fa4c7474c240e7a89ed7da5c7e3dd53.jpg");
            logo.scaleToFit(80, 80);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LineSeparator ls = new LineSeparator();
        ls.setLineColor(new BaseColor(0, 121, 107));
        document.add(new Chunk(ls));

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new BaseColor(0, 121, 107));
        Paragraph title = new Paragraph("Cartella Clinica Animale", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10);
        title.setSpacingAfter(20);
        document.add(title);

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(0, 121, 107));
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10f);
        infoTable.setSpacingAfter(20f);


        boolean alternate = false;

        alternateTableRow(infoTable, "Nome Cliente:", cliente.getFirstName(), labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Cognome Cliente:", cliente.getLastName(), labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Email Cliente:", cliente.getEmail(), labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Nome Animale:", animale.getName(), labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Specie:", animale.getSpecies(), labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Razza:", animale.getBreed(), labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Età:", String.valueOf(animale.getAge()), labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Microchip:", String.valueOf(animale.getMicrochip()), labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Peso:", animale.getWeight() != null ? animale.getWeight() + " kg" : "Non disponibile", labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Stato Clinico:", animale.getState(), labelFont, textFont, alternate);
        alternate = !alternate;
        alternateTableRow(infoTable, "Prossima Visita:", animale.getNextVisit() != null ? animale.getNextVisit().toString() : "Non programmata", labelFont, textFont, alternate);

        document.add(infoTable);

        document.add(new Chunk(ls));

        Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(0, 121, 107));

        Paragraph noteTitle = new Paragraph("Note Veterinarie", sectionTitleFont);
        noteTitle.setSpacingBefore(20f);
        noteTitle.setSpacingAfter(10f);
        document.add(noteTitle);

        Paragraph noteContent = new Paragraph(animale.getVeterinaryNotes() != null ? animale.getVeterinaryNotes() : "Nessuna nota disponibile", textFont);
        noteContent.setSpacingAfter(15f);
        document.add(noteContent);


        Paragraph symptomsTitle = new Paragraph("Sintomi", sectionTitleFont);
        symptomsTitle.setSpacingBefore(10f);
        symptomsTitle.setSpacingAfter(10f);
        document.add(symptomsTitle);

        Paragraph symptomsContent = new Paragraph(animale.getSymptoms() != null ? animale.getSymptoms() : "Nessun sintomo rilevato", textFont);
        document.add(symptomsContent);

        document.add(new Chunk(ls));
        document.add(new Paragraph("\n"));



        Paragraph somministrazioniTitle = new Paragraph("Somministrazioni", sectionTitleFont);
        somministrazioniTitle.setSpacingBefore(20f);
        somministrazioniTitle.setSpacingAfter(10f);
        document.add(somministrazioniTitle);

        List<Somministrazione> somministrazioni = somministrazioneRepository.findByAnimal(animale);

        if (somministrazioni.isEmpty()) {
            document.add(new Paragraph("Nessuna somministrazione registrata.", textFont));
        } else {
            PdfPTable somministrazioniTable = new PdfPTable(3);
            somministrazioniTable.setWidthPercentage(100);
            somministrazioniTable.setSpacingBefore(10f);

            PdfPCell header1 = new PdfPCell(new Phrase("Data", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
            PdfPCell header2 = new PdfPCell(new Phrase("Farmaco", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
            PdfPCell header3 = new PdfPCell(new Phrase("Dosaggio", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));

            BaseColor headerColor = new BaseColor(0, 121, 107);
            header1.setBackgroundColor(headerColor);
            header2.setBackgroundColor(headerColor);
            header3.setBackgroundColor(headerColor);

            header1.setHorizontalAlignment(Element.ALIGN_CENTER);
            header2.setHorizontalAlignment(Element.ALIGN_CENTER);
            header3.setHorizontalAlignment(Element.ALIGN_CENTER);

            somministrazioniTable.addCell(header1);
            somministrazioniTable.addCell(header2);
            somministrazioniTable.addCell(header3);

            for (Somministrazione s : somministrazioni) {
                String formattedDate = s.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                PdfPCell dateCell = new PdfPCell(new Phrase(formattedDate, textFont));
                PdfPCell medCell = new PdfPCell(new Phrase(s.getMedicine().getName(), textFont));
                PdfPCell doseCell = new PdfPCell(new Phrase(s.getDosage() + " unità", textFont));

                dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                medCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                doseCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                somministrazioniTable.addCell(dateCell);
                somministrazioniTable.addCell(medCell);
                somministrazioniTable.addCell(doseCell);
            }

            document.add(somministrazioniTable);
        }

        Font footerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);
        Paragraph footer = new Paragraph("Telefono: +39 012 345 6789 | Email: support@vetenterprise.com", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20f);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }


    private void alternateTableRow(PdfPTable table, String header, String value, Font headerFont, Font textFont, boolean highlight) {
        BaseColor bgColor = highlight ? new BaseColor(230, 247, 245) : BaseColor.WHITE;
        PdfPCell cell1 = new PdfPCell(new Phrase(header, headerFont));
        PdfPCell cell2 = new PdfPCell(new Phrase(value, textFont));
        cell1.setBackgroundColor(bgColor);
        cell2.setBackgroundColor(bgColor);
        cell1.setBorder(Rectangle.NO_BORDER);
        cell2.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell1);
        table.addCell(cell2);
    }



    private void addTableRow(PdfPTable table, String header, String value, Font headerFont, Font textFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(header, headerFont));
        PdfPCell cell2 = new PdfPCell(new Phrase(value, textFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell2.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell1);
        table.addCell(cell2);
    }

    private void addTableHeader(PdfPTable table, String header1, String header2, Font headerFont) {
        PdfPCell h1 = new PdfPCell(new Phrase(header1, headerFont));
        PdfPCell h2 = new PdfPCell(new Phrase(header2, headerFont));
        h1.setBackgroundColor(BaseColor.LIGHT_GRAY);
        h2.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(h1);
        table.addCell(h2);
    }

    public String createAnimalForClient(Map<String, Object> payload) {
        return adminService.createAnimalForClient(payload);
    }

    public List<Animale> getAll() {
        return animaleRepository.findAll();
    }

    @Transactional
    public void registraAnimale(NuovoAnimaleDTO dto) {

        String keycloakId = keycloakService.getUserIdByEmail(dto.getClienteEmail());

        Cliente cliente = clienteRepository.findByKeycloakId(keycloakId).orElse(null);


        if (cliente == null) {
            Utente nuovoCliente = new Utente();
            nuovoCliente.setFirstName(dto.getClienteNome());
            nuovoCliente.setLastName(dto.getClienteCognome());
            nuovoCliente.setEmail(dto.getClienteEmail());
            nuovoCliente.setUsername(dto.getClienteEmail());
            nuovoCliente.setPassword("defaultPassword");
            nuovoCliente.setRole("cliente");


            ResponseEntity<Utente> response = keycloakService.createUser(nuovoCliente);
            Utente creato = response.getBody();


            cliente = new Cliente();
            cliente.setFirstName(creato.getFirstName());
            cliente.setLastName(creato.getLastName());
            cliente.setEmail(creato.getEmail());
            cliente.setKeycloakId(creato.getKeycloakId());
            clienteRepository.save(cliente);


            notificheService.sendWelcomeNotification(creato, creato);
        }


        Reparto reparto = repartoRepository.findById(dto.getRepartoId())
                .orElseThrow(() -> new RuntimeException("Reparto non trovato"));

        List<Veterinario> veterinari = veterinarioRepository.findByReparto(reparto);
        if (veterinari.isEmpty()) {
            throw new RuntimeException("Nessun veterinario disponibile nel reparto selezionato.");
        }
        Veterinario veterinario = veterinari.get(0);

        Animale animale = new Animale();
        animale.setName(dto.getNomeAnimale());
        animale.setSpecies(dto.getSpecie());
        animale.setBreed(dto.getRazza());
        animale.setWeight(dto.getPeso());
        animale.setCliente(cliente);
        animale.setReparto(reparto);
        animale.setVeterinario(veterinario);

        animaleRepository.save(animale);


        notificheService.sendNotificationToSpecificUser(cliente.getId(),
                "È stato registrato un nuovo animale a tuo nome: " + animale.getName());
    }




}
