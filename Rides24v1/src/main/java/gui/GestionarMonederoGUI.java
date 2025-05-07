package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import businessLogic.BLFacade;

public class GestionarMonederoGUI extends JFrame{
	
	private BLFacade blFacade;
    private String userEmail;
    private JLabel saldoLabel;
    private JTextField tarjetaField, cvcField, titularField, cantidadField;
    private JLabel tarjetaLabel, cvcLabel, titularLabel, cantidadLabel; // nuevo
    private JButton cargarSaldoButton; //consultarSaldoButton;
    private JPanel panelDatosTarjeta; // nuevo
    private JButton añadirDineroButton; // nuevo
	

    public GestionarMonederoGUI(String userEmail) {

    	blFacade = MainGUI.getBusinessLogic();
        this.userEmail = userEmail;

        setTitle("Gestionar Monedero");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel principal para organizar los elementos verticalmente
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.CENTER);

        // Panel para el saldo actual
        JPanel panelSaldo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel saldoTituloLabel = new JLabel("Saldo Actual:");
        saldoTituloLabel.setFont(saldoTituloLabel.getFont().deriveFont(Font.BOLD, 16)); // Hacer el título más visible
        panelSaldo.add(saldoTituloLabel);
        saldoLabel = new JLabel("");
        saldoLabel.setFont(saldoLabel.getFont().deriveFont(Font.PLAIN, 16)); // Hacer el saldo más visible
        panelSaldo.add(saldoLabel);
        actualizarSaldo();
        panelSaldo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(panelSaldo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Espacio vertical

        // Botón "Añadir Dinero"
        añadirDineroButton = new JButton("Añadir Dinero");
        añadirDineroButton.setFont(añadirDineroButton.getFont().deriveFont(Font.PLAIN, 14)); // Hacer el botón un poco más grande
        añadirDineroButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        añadirDineroButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelDatosTarjeta.setVisible(true);
                añadirDineroButton.setVisible(false);
                pack();
                setLocationRelativeTo(null); // Centrar después de redimensionar
            }
        });
        mainPanel.add(añadirDineroButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Espacio vertical

        // Panel para los datos de la tarjeta (inicialmente oculto)
        panelDatosTarjeta = new JPanel(new GridLayout(5, 2, 10, 10)); // Más espacio entre componentes
        tarjetaLabel = new JLabel("Número de Tarjeta:");
        tarjetaField = new JTextField(16);
        cvcLabel = new JLabel("CVC:");
        cvcField = new JTextField(3);
        titularLabel = new JLabel("Titular:");
        titularField = new JTextField(30);
        cantidadLabel = new JLabel("Cantidad a Ingresar (€):");
        cantidadField = new JTextField(10);
        cargarSaldoButton = new JButton("Cargar Saldo");
        cargarSaldoButton.setFont(cargarSaldoButton.getFont().deriveFont(Font.PLAIN, 14)); // Hacer el botón más grande
        cargarSaldoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float cantidad = Float.parseFloat(cantidadField.getText());
                    String numeroTarjeta = tarjetaField.getText();
                    String cvc = cvcField.getText();
                    String titular = titularField.getText();

                    blFacade.simularCargarSaldoConTarjeta(userEmail, cantidad, numeroTarjeta, cvc, titular);
                    JOptionPane.showMessageDialog(GestionarMonederoGUI.this, "Saldo cargado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    actualizarSaldo();
                    limpiarCamposTarjeta();
                    panelDatosTarjeta.setVisible(false);
                    añadirDineroButton.setVisible(true);
                    pack();
                    setLocationRelativeTo(null); // Centrar después de redimensionar
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GestionarMonederoGUI.this, "Cantidad inválida.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panelDatosTarjeta.add(tarjetaLabel);
        panelDatosTarjeta.add(tarjetaField);
        panelDatosTarjeta.add(cvcLabel);
        panelDatosTarjeta.add(cvcField);
        panelDatosTarjeta.add(titularLabel);
        panelDatosTarjeta.add(titularField);
        panelDatosTarjeta.add(cantidadLabel);
        panelDatosTarjeta.add(cantidadField);
        panelDatosTarjeta.add(new JLabel("")); // Espacio en blanco
        panelDatosTarjeta.add(cargarSaldoButton);
        panelDatosTarjeta.setVisible(false);
        panelDatosTarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(panelDatosTarjeta);

        // Establecer un tamaño inicial más grande para la ventana
        setPreferredSize(new Dimension(400, 300));
        pack();
        setLocationRelativeTo(null);
    }

    private void actualizarSaldo() {
        float saldo = blFacade.consultarSaldo(userEmail);
        saldoLabel.setText(String.format("%.2f €", saldo));
    }

    private void limpiarCamposTarjeta() {
        tarjetaField.setText("");
        cvcField.setText("");
        titularField.setText("");
        cantidadField.setText("");
    }
}
