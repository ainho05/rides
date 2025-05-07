package gui;
import businessLogic.BLFacade;
import businessLogic.BLFacadeImplementation.BookingStatus;
import domain.Booking; // Asegúrate de tener esta clase
import gui.MainGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UserPendingBookingsGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private BLFacade facade;
    private JList<Booking> pendingBookingsList;
    private DefaultListModel<Booking> listModel;
    private JButton cancelButton;

    private String userEmail; // Necesitas tener el email del usuario actual

    public UserPendingBookingsGUI(String userEmail) {
        this.userEmail = userEmail;
        facade = MainGUI.getBusinessLogic();

        setTitle("Your Pending Bookings");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        pendingBookingsList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(pendingBookingsList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cancelButton = new JButton("Anular Viaje");
        cancelButton.setEnabled(false); // Deshabilitar al inicio hasta que se seleccione una reserva
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Cargar las reservas pendientes del usuario
        loadPendingBookings();

        // Habilitar el botón de cancelar cuando se selecciona una reserva
        pendingBookingsList.addListSelectionListener(e -> {
            cancelButton.setEnabled(!pendingBookingsList.isSelectionEmpty());
        });

        // Acción del botón "Anular Viaje"
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Booking selectedBooking = pendingBookingsList.getSelectedValue();
                if (selectedBooking != null) {
                    if (selectedBooking.getStatus() == BookingStatus.PENDING) {
                        int confirmation = JOptionPane.showConfirmDialog(
                                UserPendingBookingsGUI.this,
                                "¿Seguro que quieres anular esta reserva de viaje?",
                                "Confirmar Anulación",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (confirmation == JOptionPane.YES_OPTION) {
                            boolean cancelled = facade.cancelBooking(selectedBooking.getBookingId()); // Asumo que Booking tiene un getBookingId()
                            if (cancelled) {
                                JOptionPane.showMessageDialog(
                                        UserPendingBookingsGUI.this,
                                        "Reserva de viaje anulada correctamente.",
                                        "Anulación Exitosa",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                                loadPendingBookings(); // Recargar la lista para reflejar la anulación
                            } else {
                                JOptionPane.showMessageDialog(
                                        UserPendingBookingsGUI.this,
                                        "Error al anular la reserva de viaje.",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                UserPendingBookingsGUI.this,
                                "Solo se pueden anular reservas en estado PENDING.",
                                "Advertencia",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            UserPendingBookingsGUI.this,
                            "Por favor, selecciona una reserva para anular.",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadPendingBookings() {
        listModel.clear();
        List<Booking> pending = facade.getPendingBookingsForUser(userEmail); // Necesitas este método en BLFacade
        if (pending != null) {
            for (Booking booking : pending) {
                listModel.addElement(booking);
            }
        }
    }

    // Puedes añadir un toString() a tu clase Booking para que se muestre información útil en la lista
    // Ejemplo en Booking:
    // @Override
    // public String toString() {
    //     return "ID: " + bookingId + ", From: " + origin + ", To: " + destination + ", Status: " + status;
    // }
}