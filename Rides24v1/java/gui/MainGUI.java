package gui;

/**
 * @author Software Engineering teachers
 */


import javax.swing.*;

import domain.Driver;
import businessLogic.BLFacade;
import businessLogic.BLFacadeImplementation;
import dataAccess.DataAccess;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class MainGUI extends JFrame {
	
    private Driver driver;
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
	private JButton jButtonCreateQuery = null;
	private JButton jButtonQueryQueries = null;

    private static BLFacade appFacadeInterface;
    // IT 1
    private JButton jButtonLogin;
    private JButton jButtonRegister;
    private JButton jButtonViewBookings;
    //private JButton jButtonRequestBooking;
    private String loggedInUserEmail; // Guardará el email del usuario que hizo login
    //-
	
	public static BLFacade getBusinessLogic(){
		return appFacadeInterface;
	}
	 
	public static void setBussinessLogic (BLFacade afi){
		appFacadeInterface=afi;
	}
	protected JLabel jLabelSelectOption;
	private JRadioButton rdbtnNewRadioButton;
	private JRadioButton rdbtnNewRadioButton_1;
	private JRadioButton rdbtnNewRadioButton_2;
	private JPanel panel;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private  JButton btnLeaveReview;	
	
	// IT3 (crear botón para la gestión del monedero)
	private JButton gestionarMonederoButton;
	private JButton jButtonViewPendingBookings; // botón para ver las bookings pendientes
	
	
	/**
	 * This is the default constructor
	 */
	public MainGUI() {
		super();
	    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	        if (appFacadeInterface != null) {
	           DataAccess.closeFactory();
	          
	        }
	        System.out.println("EntityManagerFactory cerrado correctamente");
	    }));
		//driver=d;
		
		// this.setSize(271, 295);
		this.setSize(495, 290);
		//jLabelSelectOption = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.SelectOption"));
		jLabelSelectOption = new JLabel("Seleccionar opción");
		jLabelSelectOption.setFont(new Font("Tahoma", Font.BOLD, 13));
		jLabelSelectOption.setForeground(Color.BLACK);
		jLabelSelectOption.setHorizontalAlignment(SwingConstants.CENTER);
		
		rdbtnNewRadioButton = new JRadioButton("English");
		rdbtnNewRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Locale.setDefault(new Locale("en"));
				System.out.println("Locale: "+Locale.getDefault());
				paintAgain();				}
		});
		buttonGroup.add(rdbtnNewRadioButton);
		
		rdbtnNewRadioButton_1 = new JRadioButton("Euskara");
		rdbtnNewRadioButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Locale.setDefault(new Locale("eus"));
				System.out.println("Locale: "+Locale.getDefault());
				paintAgain();				}
		});
		buttonGroup.add(rdbtnNewRadioButton_1);
		
		rdbtnNewRadioButton_2 = new JRadioButton("Castellano");
		rdbtnNewRadioButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Locale.setDefault(new Locale("es"));
				System.out.println("Locale: "+Locale.getDefault());
				paintAgain();
			}
		});
		buttonGroup.add(rdbtnNewRadioButton_2);
	
		panel = new JPanel();
		panel.add(rdbtnNewRadioButton_1);
		panel.add(rdbtnNewRadioButton_2);
		panel.add(rdbtnNewRadioButton);
		
		// Botón para crear viaje (solo Drivers)
		jButtonCreateQuery = new JButton();
		jButtonCreateQuery.setText(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.CreateRide"));
		
		jButtonCreateQuery.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				BLFacade facade = MainGUI.getBusinessLogic();
		        
		        // Verificar si el usuario actual es un conductor
		        if (loggedInUserEmail == null || !facade.isDriver(loggedInUserEmail)) {
		            JOptionPane.showMessageDialog(MainGUI.this, 
		                "You must be logged in as a driver to create rides", 
		                "Error", JOptionPane.ERROR_MESSAGE);
		            return;
		        }
		        
		        // Obtener el conductor actual desde la base de datos
		        Driver currentDriver = facade.getDriver(loggedInUserEmail);
		        if (currentDriver == null) {
		            JOptionPane.showMessageDialog(MainGUI.this, 
		                "Driver data not found", 
		                "Error", JOptionPane.ERROR_MESSAGE);
		            return;
		        }
				
				JFrame a = new CreateRideGUI(currentDriver);
				a.setVisible(true);
			}
		});
		jButtonCreateQuery.setVisible(false); // primero hay que hacer login
		
		// botón para buscar viajes (solo Users)
		jButtonQueryQueries = new JButton();
		jButtonQueryQueries.setText(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.QueryRides"));
		jButtonQueryQueries.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				/*if (loggedInUserEmail == null || loggedInUserEmail.isEmpty()) {
		            JOptionPane.showMessageDialog(null, "You must log in first!", "Error", JOptionPane.ERROR_MESSAGE);
		            return;
		        }*/
				
				JFrame findRidesFrame = new FindRidesGUI(loggedInUserEmail);

				findRidesFrame.setVisible(true);
			}
		});
		//jButtonQueryQueries.setVisible(false); // no visible hasta que no se haga login
		
		jContentPane = new JPanel(new BorderLayout());
		JPanel panelCenter = new JPanel(new GridLayout(4, 1, 10, 10));
        panelCenter.add(jLabelSelectOption);
        panelCenter.add(jButtonCreateQuery);
        panelCenter.add(jButtonQueryQueries);
        panelCenter.add(panel);

        jContentPane.add(panelCenter, BorderLayout.CENTER);
		
		
		setContentPane(jContentPane);
		setTitle(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.MainTitle") + "PestañaPrincipal");
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(1);
			}
		});
		
		// IT 1
		// Botón para Login
		jButtonLogin = new JButton("Login");
		jButtonLogin.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        JFrame loginFrame = new LoginGUI(MainGUI.this);
		        loginFrame.setVisible(true);
		    }
		});

		// Botón para Registro
		jButtonRegister = new JButton("Register");
		jButtonRegister.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        JFrame registerFrame = new RegisterGUI();
		        registerFrame.setVisible(true);
		    }
		});
		
		// botón para visualizar requests de bookings (para Drivers)
		jButtonViewBookings = new JButton("View Booking Requests");
		jButtonViewBookings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (loggedInUserEmail == null || loggedInUserEmail.isEmpty()) {
	                JOptionPane.showMessageDialog(null, "You must log in first!", "Error", JOptionPane.ERROR_MESSAGE);
	                return;
	            }
	            JFrame viewBookingsFrame = new ViewBookingsGUI(loggedInUserEmail);
	            viewBookingsFrame.setVisible(true);
			} 
        });
        jButtonViewBookings.setVisible(false);
        
		// IT2 -> CU: Reviews
        btnLeaveReview = new JButton("Leave a Review");
        btnLeaveReview.setVisible(false); // Inicialmente oculto IT3

        btnLeaveReview.addActionListener(e -> {
            if (loggedInUserEmail == null || loggedInUserEmail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must be logged in to leave a review", "Not Logged In", JOptionPane.WARNING_MESSAGE);
                return;
            }
         // Verificar si el usuario ha completado algún viaje
            BLFacade blFacade = new BLFacadeImplementation();
            if (!blFacade.hasPassengerTraveled(loggedInUserEmail)) {
                JOptionPane.showMessageDialog(this, "You have not completed any trips and cannot leave a review.", "No Completed Trips", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            JFrame reviewFrame = new ReviewGUI(loggedInUserEmail);
            reviewFrame.setVisible(true);
        });
        // Añadir el botón al panel o al contenedor adecuado
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnLeaveReview);
        

        // Añadir el panel con el botón a la ventana
        jContentPane.add(buttonPanel, BorderLayout.SOUTH);
		
        /*// botón para solicitar viaje (Usuarios)
		jButtonRequestBooking = new JButton("Request a Ride");
		jButtonRequestBooking.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    if (loggedInUserEmail == null || loggedInUserEmail.isEmpty()) {
			        JOptionPane.showMessageDialog(null, "You must log in first!", "Error", JOptionPane.ERROR_MESSAGE);
			        return;
			    }
	
			    JFrame requestBookingFrame = new RequestBookingGUI(loggedInUserEmail);
			    requestBookingFrame.setVisible(true);
			}
		});
		jButtonRequestBooking.setVisible(false);*/

		

		// Panel para los botones de autenticación
        JPanel panelAuth = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelAuth.add(jButtonLogin);
        panelAuth.add(jButtonRegister);
        jContentPane.add(panelAuth, BorderLayout.NORTH);

        // Panel para los botones de funcionalidad después del login
        JPanel panelActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelActions.add(jButtonViewBookings);
        //panelActions.add(jButtonRequestBooking);
        jContentPane.add(panelActions, BorderLayout.SOUTH);
        
        panelActions.add(btnLeaveReview);

        setContentPane(jContentPane);
        
     // IT3 (inicializar el botón y establecer el texto)
        gestionarMonederoButton = new JButton("Mi monedero");
        gestionarMonederoButton.setVisible(false); // Inicialmente oculto
        
        panelActions.add(gestionarMonederoButton); // para añadir el botón a la interfaz
        
        gestionarMonederoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BLFacade blFacade = MainGUI.getBusinessLogic();
                String currentUserEmail = loggedInUserEmail;

                if (currentUserEmail != null && !currentUserEmail.isEmpty() && blFacade != null) {
                	
                	GestionarMonederoGUI monederoGUI = new GestionarMonederoGUI(currentUserEmail);
                    monederoGUI.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(MainGUI.this, "Debes iniciar sesión para gestionar tu monedero.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        jButtonViewPendingBookings = new JButton("Mis Reservas Pendientes");
        jButtonViewPendingBookings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loggedInUserEmail == null || loggedInUserEmail.isEmpty()) {
                    JOptionPane.showMessageDialog(MainGUI.this, "Debes iniciar sesión para ver tus reservas pendientes.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                UserPendingBookingsGUI pendingBookingsGUI = new UserPendingBookingsGUI(loggedInUserEmail);
                pendingBookingsGUI.setVisible(true);
            }
        });
        jButtonViewPendingBookings.setVisible(false); // Inicialmente oculto hasta que el usuario inicie sesión
        panelActions.add(jButtonViewPendingBookings);


	}
	
	public void setLoggedInUserEmail(String email) {
	    this.loggedInUserEmail = email;
	    System.out.println("Usuario guardado en MainGUI: " + email);

	    // Verificar si los botones ya han sido inicializados
	    //if (jButtonViewBookings == null || jButtonRequestBooking == null) {
	    if (jButtonViewBookings == null) {
	    	System.out.println("Error: Los botones no han sido inicializados correctamente.");
	        return;
	    }

	    // Verificar si el usuario es Driver o User
	    BLFacade facade = MainGUI.getBusinessLogic();
	    boolean isDriver = facade.isDriver(email);

	    if (isDriver) {
	        jButtonViewBookings.setVisible(true);  // Mostrar "View Booking Requests" para Drivers
	        //jButtonRequestBooking.setVisible(false); // Ocultar "Request a Ride"
	        jButtonCreateQuery.setVisible(true);
	        jButtonQueryQueries.setVisible(true);
	        jButtonViewPendingBookings.setVisible(false); // Ocultar para conductores IT3
	        btnLeaveReview.setVisible(false); // IT3
	        gestionarMonederoButton.setVisible(true); // IT3
	    } else {
	        jButtonViewBookings.setVisible(false); // Ocultar "View Booking Requests"
	        //jButtonRequestBooking.setVisible(true);  // Mostrar "Request a Ride" para Users
	        jButtonQueryQueries.setVisible(true);
	        jButtonCreateQuery.setVisible(false);
	        jButtonViewPendingBookings.setVisible(true);  // Mostrar para usuarios IT3
	        btnLeaveReview.setVisible(true); // IT3 
	        gestionarMonederoButton.setVisible(true); // IT3
	    }
	    
	    /*// Ocultar botones de login y registro después de iniciar sesión
        jButtonLogin.setVisible(false);
        jButtonRegister.setVisible(false); */


	    // Refrescar la interfaz
	    revalidate();
	    repaint();
	}


	
	private void paintAgain() {
		jLabelSelectOption.setText(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.SelectOption"));

		jButtonQueryQueries.setText(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.QueryRides"));
		jButtonCreateQuery.setText(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.CreateRide"));
		this.setTitle(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.MainTitle")+ " - driver :"+driver.getName());
		
	}
	


	
} // @jve:decl-index=0:visual-constraint="0,0"

