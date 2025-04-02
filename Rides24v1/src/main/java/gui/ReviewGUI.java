package gui;

import businessLogic.BLFacade;
import domain.Review;
import domain.Driver;
import gui.MainGUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ReviewGUI extends JFrame {
	
	private static final long serialVersionUID = 1L;
    private BLFacade facade;
    private String passengerEmail;
    private JComboBox<Driver> driverComboBox;
    
    public ReviewGUI(String passengerEmail) {
    	this.passengerEmail = passengerEmail;
        this.facade = MainGUI.getBusinessLogic();

        setTitle("Leave a Review");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Panel para la reseña
        JPanel reviewPanel = new JPanel();
        reviewPanel.setLayout(new GridLayout(5, 1));
        reviewPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblDriver = new JLabel("Select Driver:");
        reviewPanel.add(lblDriver);

        // Obtener la lista de conductores con los que el pasajero ha viajado
        List<Driver> drivers = facade.getDriversForPassenger(passengerEmail);
        driverComboBox = new JComboBox<>();
        
     // Añadir conductores al JComboBox solo si el pasajero ha viajado con ellos
        if (drivers != null && !drivers.isEmpty()) {
            for (Driver driver : drivers) {
                driverComboBox.addItem(driver); // Añadir el objeto Driver completo
            }
        } else {
            // Si no hay conductores disponibles, mostrar un mensaje o hacer el combo vacío
        	driverComboBox.addItem(new Driver("No drivers available", ""));
        }
        reviewPanel.add(driverComboBox);
        
        driverComboBox.setRenderer(new ListCellRenderer<Driver>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Driver> list, Driver value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                // Mostrar solo el nombre del conductor en la lista
                JLabel label = new JLabel(value.getName());
                if (isSelected) {
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                } else {
                    label.setBackground(list.getBackground());
                    label.setForeground(list.getForeground());
                }
                return label;
            }
        });

        JLabel lblRating = new JLabel("Rating (1-5):");
        reviewPanel.add(lblRating);

        JComboBox<Integer> comboBoxRating = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        reviewPanel.add(comboBoxRating);

        getContentPane().add(reviewPanel, BorderLayout.CENTER);

        // Botón para enviar la reseña
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton btnSubmit = new JButton("Submit Review");
        btnSubmit.addActionListener(e -> {
        	Driver selectedDriver = (Driver) driverComboBox.getSelectedItem();
        	String driverEmail = selectedDriver.getEmail();
            int rating = (Integer) comboBoxRating.getSelectedItem();

            Review review = new Review(passengerEmail, driverEmail, rating);
            /*facade.submitReview(review);
            JOptionPane.showMessageDialog(this, "Review submitted successfully!");
            */
            try {
            	if (facade.hasPassengerTraveledWithDriver(passengerEmail, driverEmail)) {
            		facade.submitReview(review);
            		
            		JOptionPane.showMessageDialog(this, "Reseña guardada con éxito.");
            		
            	}
            	else {
            		JOptionPane.showMessageDialog(this, "No has viajado con este conductor, no puedes dejar una reseña.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
            	}
            } catch (IllegalStateException e2) {
                JOptionPane.showMessageDialog(this, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            dispose(); // Cierra la ventana después de enviar la reseña
        });
        
        buttonPanel.add(btnSubmit);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }
}