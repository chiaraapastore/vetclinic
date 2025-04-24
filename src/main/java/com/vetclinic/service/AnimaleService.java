package com.vetclinic.service;

import com.vetclinic.models.*;
import com.vetclinic.repository.*;
import com.vetclinic.config.AuthenticationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.*;
import java.io.ByteArrayOutputStream;
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
    private final EsameRepository esameRepository;
    private final OperazioneRepository operazioneRepository;

    public AnimaleService(AdminService adminService,TrattamentoRepository trattamentoRepository, EsameRepository esameRepository, OperazioneRepository operazioneRepository,AnimaleRepository animaleRepository, ClienteRepository clienteRepository, FatturaRepository fatturaRepository,AuthenticationService authenticationService) {
        this.animaleRepository = animaleRepository;
        this.clienteRepository = clienteRepository;
        this.authenticationService = authenticationService;
        this.fatturaRepository = fatturaRepository;
        this.adminService = adminService;
        this.trattamentoRepository = trattamentoRepository;
        this.esameRepository = esameRepository;
        this.operazioneRepository = operazioneRepository;
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
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
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

        Paragraph operationsTitle = new Paragraph("Operazioni Eseguite", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(0, 121, 107)));
        operationsTitle.setAlignment(Element.ALIGN_LEFT);
        document.add(operationsTitle);

        // Use findByAnimaleId to get the operations
        List<Operazione> operations = operazioneRepository.findByAnimaleId(animaleId);
        if (operations.isEmpty()) {
            document.add(new Paragraph("Nessuna operazione eseguita", textFont));
        } else {
            PdfPTable operationsTable = new PdfPTable(2);
            operationsTable.setWidthPercentage(100);
            addTableHeader(operationsTable, "Tipo di Operazione", "Data", headerFont);

            for (Operazione operation : operations) {
                addTableRow(operationsTable, operation.getTipoOperazione(), operation.getDataOra().toString(), textFont, textFont);
            }
            document.add(operationsTable);
        }

        document.close();
        return baos.toByteArray();
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

}
