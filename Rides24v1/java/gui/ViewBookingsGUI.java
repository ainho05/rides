package gui;

import businessLogic.BLFacade;
import domain.Booking;
import domain.Ride;
import domain.User;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication; 
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;

public class ViewBookingsGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;
    private String driverEmail;
    private BLFacade facade;
    // botones para aceptar o rechazar la reserva
    private JButton btnAccept; 
    private JButton btnReject;

    public ViewBookingsGUI(String driverEmail) {
        this.driverEmail = driverEmail;
        this.facade = MainGUI.getBusinessLogic(); // Obtener la lógica de negocio

        if (facade == null) {
        	JOptionPane.showMessageDialog(null, "Error: BLFacade no está inicializado.");
        	return;
        }
        
        setTitle("Booking Requests");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // para etiquetar el título
        JLabel lblTitle = new JLabel("Booking Requests for Your Rides");
        lblTitle.setBounds(180, 10, 300, 30);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitle, BorderLayout.NORTH);

        // Definir columnas de la tabla
        String[] columnNames = {"Passenger", "From", "To", "Date", "Seats"};

        // Modelo de tabla
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);
        
     // Panel para botones de aceptar/rechazar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        btnAccept = new JButton("Accept");
        btnAccept.setEnabled(false);
        btnAccept.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleBookingDecision(true);
            }
        });
        
        btnReject = new JButton("Reject");
        btnReject.setEnabled(false);
        btnReject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleBookingDecision(false);
            }
        });

        buttonPanel.add(btnAccept);
        buttonPanel.add(btnReject);
        add(buttonPanel, BorderLayout.SOUTH);

        loadBookings(); // Cargar los datos en la tabla
        
     // Habilitar los botones solo cuando se selecciona una solicitud
        table.getSelectionModel().addListSelectionListener(event -> {
            boolean rowSelected = table.getSelectedRow() != -1;
            btnAccept.setEnabled(rowSelected);
            btnReject.setEnabled(rowSelected);
        });

        setLocationRelativeTo(null);
    }

    private void loadBookings() {
    	
    	if (facade == null) {
    		System.out.println("No se pudo cargar las reservas. facade es null.");
    	}
    	
        tableModel.setRowCount(0); // Limpiar la tabla

        List<Booking> bookings = facade.viewBookingsForDriver(driverEmail);
        if (bookings == null || bookings.isEmpty()) {
            System.out.println("No hay reservas para este conductor.");
            return;
        }
        for (Booking booking : bookings) {
        	// solo miramos reservas pendientes
        	if (!booking.isConfirmed()) { 
                User passenger = booking.getUser();
                Ride ride = booking.getRide();

                tableModel.addRow(new Object[]{
                    passenger.getEmail(),  // Nombre del pasajero
                    ride.getFrom(),        // Origen del viaje
                    ride.getTo(),          // Destino del viaje
                    ride.getDate(),        // Fecha del viaje
                    1                      // 1 asiento reservado (puedes cambiar esto si hay más info)
                });
            }
        }
        	
    }
    private void handleBookingDecision(boolean accepted) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking request.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String passengerEmail = (String) tableModel.getValueAt(selectedRow, 0);
        boolean success;
        
        if (accepted) {
            success = facade.acceptBookingRequest(driverEmail, passengerEmail);
        } else {
            success = facade.rejectBookingRequest(driverEmail, passengerEmail);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, accepted ? "Booking accepted!" : "Booking rejected.");
            //nuevo it3
            enviarCorreo(driverEmail, passengerEmail);

            loadBookings(); // Recargar la lista después de la acción
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update booking request.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    //nuevo metodo it3
    private void enviarCorreo(String remitente, String destinatario) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        "correo1pruebas2025@gmail.com", // Tu correo de app
                        "ziziyaxrjbpufvcz"  // Contraseña de aplicación
                    );
                }
            });

            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(remitente));
            email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            email.setSubject("¡Viaje aceptado!");
            email.setText(String.format(
                "Hola,\n\nEl conductor/a %s ha aceptado tu solicitud de viaje.\n\nSaludos, gracias por elegirnos,\n Equipo ViajeAstral AP",
                remitente
            ));

            Transport.send(email);
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando notificación: " + e.getMessage());
            // Puedes mostrar un aviso opcional:
            JOptionPane.showMessageDialog(this, 
                "Reserva aceptada, pero falló el envío de notificación.", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
        }

}}
