package gui;

import businessLogic.BLFacade;
import domain.Ride;
import gui.MainGUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RequestBookingGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;
    private String passengerEmail;
    private BLFacade facade;
    private JComboBox<Integer> comboBox;
    
    public RequestBookingGUI(String passengerEmail, Ride selectedRide) {
        this.passengerEmail = passengerEmail;
        this.facade = MainGUI.getBusinessLogic();

        setTitle("Request a Ride");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Mostrar detalles del viaje seleccionado
        JPanel rideInfoPanel = new JPanel();
        rideInfoPanel.setLayout(new GridLayout(5, 1));
        rideInfoPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // para corregir la posición
        
        rideInfoPanel.add(new JLabel("From: " + selectedRide.getFrom()));
        rideInfoPanel.add(new JLabel("To: " + selectedRide.getTo()));
        rideInfoPanel.add(new JLabel("Date: " + selectedRide.getDate()));
        rideInfoPanel.add(new JLabel("Available Seats: " + selectedRide.getAvailableSeats()));
        rideInfoPanel.add(new JLabel("Price: " + selectedRide.getPrice()));

        getContentPane().add(rideInfoPanel, BorderLayout.CENTER);
        //IT2- opcion para poder reservar más de un asiento
        int avaliableSeats= selectedRide.getAvailableSeats();
        //como máximo se podrán comprar 5 seats
        Integer[] seatNumbers =new Integer[Math.min(avaliableSeats, 5)];
        for(int i=0;i<seatNumbers.length;i++) {
        	seatNumbers[i]=i+1;
        }
        comboBox =new JComboBox<>(seatNumbers);
        comboBox.setMaximumRowCount(5);
        rideInfoPanel.add(comboBox);
        

        // Botón para confirmar la reserva
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(400, 80)); // Asegura que el panel tenga espacio
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JButton btnConfirm = new JButton("Confirm Booking");
        btnConfirm.setBounds(100, 200, 300, 80);
        
        btnConfirm.addActionListener(e -> {
        	if(passengerEmail == null || passengerEmail.isEmpty()) {
        		 JOptionPane.showMessageDialog(this, "You must be logged in to make a reservation",
        		"Not Logged In", JOptionPane.WARNING_MESSAGE);
        		 return;
        		 }
            int numSeats=(Integer) comboBox.getSelectedItem();
        	boolean resquestSent = facade.requestBooking(passengerEmail, selectedRide.getRideNumber(),numSeats);
            if (resquestSent) {
                JOptionPane.showMessageDialog(this, "Booking request sent! Wait for driver approval");
                dispose(); // Cierra la ventana después de reservar
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send booking request", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnConfirm); // Agregar botón al panel
        add(buttonPanel, BorderLayout.SOUTH); // Agregar el panel con el botón a la ventana

        setLocationRelativeTo(null);
    }


    private void loadAvailableRides() {
        tableModel.setRowCount(0);
        List<Ride> rides = facade.getAllAvailableRides(); // Necesitas implementar este método en BLFacade

        for (Ride ride : rides) {
            tableModel.addRow(new Object[]{
                ride.getRideNumber(),
                ride.getFrom(),
                ride.getTo(),
                ride.getDate(),
                ride.getAvailableSeats(),
                ride.getPrice()
            });
        }
    }

    private void reserveRide() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ride", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int rideId = (int) tableModel.getValueAt(selectedRow, 0);
        int numSeats=(Integer) comboBox.getSelectedItem();
        boolean success = facade.requestBooking(passengerEmail, rideId,numSeats);

        if (success) {
            JOptionPane.showMessageDialog(this, "Ride reserved successfully!");
            loadAvailableRides(); // Actualizar la lista
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reserve ride", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

