package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import businessLogic.BLFacade;
import domain.Driver;
import domain.User;
import dataAccess.DataAccess;

public class RegisterGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField txtUsername, txtEmail;
    private JPasswordField txtPassword;
    private JButton btnRegister, btnSelectProfilePicture;
    private JLabel lblUsername, lblEmail, lblPassword, lblPreview;
    private JRadioButton radioButtonDriver;
    private File selectedImageFile;
    private BLFacade facade;

    public RegisterGUI() {
        setTitle("Register");
        setSize(350, 350); // Aumentamos el tamaño inicial
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espacio entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        lblUsername = new JLabel("Username:");
        getContentPane().add(lblUsername, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        txtUsername = new JTextField(15); // Establecer un tamaño preferido
        getContentPane().add(txtUsername, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        lblEmail = new JLabel("Email:");
        getContentPane().add(lblEmail, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        txtEmail = new JTextField(15);
        getContentPane().add(txtEmail, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        lblPassword = new JLabel("Password:");
        getContentPane().add(lblPassword, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        txtPassword = new JPasswordField(15);
        getContentPane().add(txtPassword, gbc);

        // Select Picture Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        btnSelectProfilePicture = new JButton("Select Picture");
        getContentPane().add(btnSelectProfilePicture, gbc);

        // Preview Label
        gbc.gridx = 1;
        gbc.gridy = 3;
        lblPreview = new JLabel();
        lblPreview.setPreferredSize(new Dimension(80, 80));
        lblPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        getContentPane().add(lblPreview, gbc);

        // Driver Radio Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2; // Ocupa dos columnas
        radioButtonDriver = new JRadioButton("Register as a driver");
        getContentPane().add(radioButtonDriver, gbc);
        gbc.gridwidth = 1; // Restablecer el ancho de la columna

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2; // Ocupa dos columnas
        btnRegister = new JButton("Register");
        getContentPane().add(btnRegister, gbc);

        facade = MainGUI.getBusinessLogic();

        btnSelectProfilePicture.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif");
                fileChooser.setFileFilter(filter);
                int returnVal = fileChooser.showOpenDialog(RegisterGUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    selectedImageFile = fileChooser.getSelectedFile();
                    ImageIcon icon = new ImageIcon(selectedImageFile.getAbsolutePath());
                    Image scaledImage = icon.getImage().getScaledInstance(lblPreview.getWidth(), lblPreview.getHeight(), Image.SCALE_SMOOTH);
                    lblPreview.setIcon(new ImageIcon(scaledImage));
                    lblPreview.setText("");
                    btnRegister.setEnabled(true);
                } else {
                    btnRegister.setEnabled(false);
                }
            }
        });

        btnRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText().trim();
                String email = txtEmail.getText().trim();
                String password = new String(txtPassword.getPassword()).trim();
                boolean isDriver = radioButtonDriver.isSelected();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (selectedImageFile == null) {
                    JOptionPane.showMessageDialog(null, "Please select a profile picture.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DataAccess dataAccess = new DataAccess();
                byte[] profilePicture = null;
                try {
                    profilePicture = Files.readAllBytes(selectedImageFile.toPath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error reading profile picture.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!dataAccess.userExists(email)) {
                    if (isDriver) {
                        Driver driv = new Driver(email, password, username, profilePicture);
                        dataAccess.addDriver(driv);
                    } else {
                        User us = new User(username, email, password, profilePicture);
                        dataAccess.addUsertobd(us);
                    }
                    JOptionPane.showMessageDialog(null, "Registration Successful");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "User already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setLocationRelativeTo(null);
        pack(); // Ajusta el tamaño de la ventana al de sus componentes
    }
}
