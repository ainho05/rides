package gui;

import businessLogic.BLFacade;
import domain.Booking;
import domain.Ride;
import domain.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
            loadBookings(); // Recargar la lista después de la acción
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update booking request.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
