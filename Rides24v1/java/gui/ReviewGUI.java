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
    // IT3
    private JComboBox<Integer> comboBoxRating;
    private JComboBox<Integer> comboBoxPunctuality;
    private JComboBox<Integer> comboBoxComfort;
    private JComboBox<Integer> comboBoxAttitude;
    private JTextArea txtComment;
    
    
    public ReviewGUI(String passengerEmail) {
    	this.passengerEmail = passengerEmail;
        this.facade = MainGUI.getBusinessLogic();

        setTitle("Leave a Review");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Panel para la reseña
        JPanel reviewPanel = new JPanel();
        reviewPanel.setLayout(new GridLayout(7, 2, 5, 5));
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
        
        // IT3 (todo el código de abajo)
        driverComboBox.setRenderer(new DriverListCellRenderer());

        JLabel lblRating = new JLabel("Overall Rating (1-5):");
        reviewPanel.add(lblRating);
        comboBoxRating = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        reviewPanel.add(comboBoxRating);

        JLabel lblPunctuality = new JLabel("Punctuality (1-5):");
        reviewPanel.add(lblPunctuality);
        comboBoxPunctuality = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        reviewPanel.add(comboBoxPunctuality);

        JLabel lblComfort = new JLabel("Car Comfort (1-5):");
        reviewPanel.add(lblComfort);
        comboBoxComfort = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        reviewPanel.add(comboBoxComfort);

        JLabel lblAttitude = new JLabel("Driver Attitude (1-5):");
        reviewPanel.add(lblAttitude);
        comboBoxAttitude = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        reviewPanel.add(comboBoxAttitude);

        JLabel lblComment = new JLabel("Comments:");
        reviewPanel.add(lblComment);
        txtComment = new JTextArea(3, 15);
        JScrollPane commentScrollPane = new JScrollPane(txtComment);
        reviewPanel.add(commentScrollPane);

        getContentPane().add(reviewPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSubmit = new JButton("Submit Review");
        btnSubmit.addActionListener(e -> {
            Driver selectedDriver = (Driver) driverComboBox.getSelectedItem();
            if (selectedDriver == null || selectedDriver.getEmail().equals("No drivers available")) {
                JOptionPane.showMessageDialog(this, "Please select a driver.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String driverEmail = selectedDriver.getEmail();
            int rating = (Integer) comboBoxRating.getSelectedItem();
            int punctuality = (Integer) comboBoxPunctuality.getSelectedItem();
            int comfort = (Integer) comboBoxComfort.getSelectedItem();
            int attitude = (Integer) comboBoxAttitude.getSelectedItem();
            String comment = txtComment.getText().trim();

            Review review = new Review(passengerEmail, driverEmail, rating, punctuality, comfort, attitude, comment);

            try {
                if (facade.hasPassengerTraveledWithDriver(passengerEmail, driverEmail)) {
                    facade.submitReview(review);
                    JOptionPane.showMessageDialog(this, "Detailed review saved successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "You haven't traveled with this driver.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalStateException e2) {
                JOptionPane.showMessageDialog(this, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            dispose();
        });
        buttonPanel.add(btnSubmit);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private static class DriverListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Driver) {
                value = ((Driver) value).getName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
