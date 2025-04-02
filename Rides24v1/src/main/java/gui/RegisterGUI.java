package gui;

import javax.swing.*;
import dataAccess.DataAccess; // Asegúrate de que esta línea esté presente
import domain.Driver; // Asegúrate de que esta línea esté presente
import domain.User;
import businessLogic.BLFacade;
import dataAccess.DataAccess;
import domain.Driver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField txtUsername, txtEmail;
    private JPasswordField txtPassword;
    private JButton btnRegister;
    private BLFacade facade;

    public RegisterGUI() {
        setTitle("Register");
        setSize(300, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setBounds(30, 30, 80, 25);
        getContentPane().add(lblUsername);

        txtUsername = new JTextField();
        txtUsername.setBounds(120, 30, 150, 25);
        getContentPane().add(txtUsername);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setBounds(30, 70, 80, 25);
        getContentPane().add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(120, 70, 150, 25);
        getContentPane().add(txtEmail);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(30, 110, 80, 25);
        getContentPane().add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(120, 110, 150, 25);
        getContentPane().add(txtPassword);

        btnRegister = new JButton("Register");
        btnRegister.setBounds(93, 161, 120, 30);
        getContentPane().add(btnRegister);
        
        JRadioButton radioButtonDriver = new JRadioButton("Click to register as a driver");
        radioButtonDriver.setBounds(6, 141, 165, 25);
        getContentPane().add(radioButtonDriver);
        
        

        facade = MainGUI.getBusinessLogic(); // Obtener lógica de negocio

        /*btnRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText();
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());
                
                if (facade.register(username, password, email)) {
                    JOptionPane.showMessageDialog(null, "Registration Successful");
                    dispose(); // Cierra la ventana de registro
                } else {
                    JOptionPane.showMessageDialog(null, "Error in registration", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });*/
        
        btnRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText().trim();
                String email = txtEmail.getText().trim();
                String password = new String(txtPassword.getPassword()).trim();
                
                boolean isDriver = radioButtonDriver.isSelected(); // Si está seleccionado = driver

                // Validación de campos vacíos
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Crear una instancia de DataAccess
                DataAccess dataAccess = new DataAccess();

                // Verificar si el usuario ya existe
                if (!dataAccess.userExists(email)) {  
                    if (isDriver) {
                        Driver driv = new Driver(email, password, username);
                        dataAccess.addDriver(driv);
                    } else {
                        User us = new User(username, email, password);
                        dataAccess.addUsertobd(us);
                    }
                    JOptionPane.showMessageDialog(null, "Registration Successful");
                    dispose(); // Cierra la ventana de registro
                } else {
                    JOptionPane.showMessageDialog(null, "User  already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        setLocationRelativeTo(null);

    }
}