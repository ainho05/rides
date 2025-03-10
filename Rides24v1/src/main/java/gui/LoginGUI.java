package gui;

import businessLogic.BLFacade;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private BLFacade facade;
    private MainGUI mainGUI; // Referencia a la ventana principal

    public LoginGUI(MainGUI mainGUI) { // Recibir MainGUI como parámetro
        this.mainGUI = mainGUI;
        this.facade = MainGUI.getBusinessLogic();

        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel lblUsername = new JLabel("Email:");
        lblUsername.setBounds(30, 30, 80, 25);
        add(lblUsername);

        txtEmail = new JTextField();
        txtEmail.setBounds(120, 30, 150, 25);
        add(txtEmail);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(30, 70, 80, 25);
        add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(120, 70, 150, 25);
        add(txtPassword);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(90, 110, 120, 30);
        add(btnLogin);

        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = txtEmail.getText().trim();
                String password = new String(txtPassword.getPassword()).trim();

                if (email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter both username and password.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (facade.login(email, password)) {
                    JOptionPane.showMessageDialog(null, "Login Successful");

                    // 🔹 Ahora podemos guardar el usuario en MainGUI
                    if (mainGUI != null) {
                        mainGUI.setLoggedInUserEmail(email);
                        System.out.println("✅ Usuario guardado en MainGUI: " + email);
                    } else {
                        System.out.println("❌ No se pudo guardar el usuario en MainGUI");
                    }

                    dispose(); // Cierra la ventana de login
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Credentials", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        setLocationRelativeTo(null);
    }
}
