package gui;

import businessLogic.BLFacade;
import configuration.UtilDate;

import com.toedter.calendar.JCalendar;
import domain.Ride;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

import javax.swing.table.DefaultTableModel;


public class FindRidesGUI extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private String loggedInUserEmail; // nueva var para almacenar el mail del usuario


	private JComboBox<String> jComboBoxOrigin = new JComboBox<String>();
	DefaultComboBoxModel<String> originLocations = new DefaultComboBoxModel<String>();

	private JComboBox<String> jComboBoxDestination = new JComboBox<String>();
	DefaultComboBoxModel<String> destinationCities = new DefaultComboBoxModel<String>();

	private JLabel jLabelOrigin = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.LeavingFrom"));
	private JLabel jLabelDestination = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.GoingTo"));
	private final JLabel jLabelEventDate = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.RideDate"));
	private final JLabel jLabelEvents = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.Rides")); 

	private JButton jButtonClose = new JButton(ResourceBundle.getBundle("Etiquetas").getString("Close"));

	// Code for JCalendar
	private JCalendar jCalendar1 = new JCalendar();
	private Calendar calendarAnt = null;
	private Calendar calendarAct = null;
	private JScrollPane scrollPaneEvents = new JScrollPane();

	private List<Date> datesWithRidesCurrentMonth = new Vector<Date>();

	private JTable tableRides= new JTable();

	private DefaultTableModel tableModelRides;


	private String[] columnNamesRides = new String[] {
			ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.Driver"), 
			ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.NPlaces"), 
			ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.Price")
	};

	private JRadioButton buttonOrdenarPrecio;

	private JRadioButton buttonOrdenarAsiento;
	private ButtonGroup grupoOrden;
	


	public FindRidesGUI(String userEmail)
	{
		
		this.loggedInUserEmail = userEmail; 	

		this.getContentPane().setLayout(null);
		this.setSize(new Dimension(700, 500));
		this.setTitle(ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.FindRides"));

		jLabelEventDate.setBounds(new Rectangle(457, 6, 140, 25));
		jLabelEvents.setBounds(172, 229, 259, 16);

		this.getContentPane().add(jLabelEventDate, null);
		this.getContentPane().add(jLabelEvents);

		jButtonClose.setBounds(new Rectangle(182, 417, 130, 30));

		jButtonClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jButton2_actionPerformed(e);
			}
		});
		BLFacade facade = MainGUI.getBusinessLogic();
		List<String> origins=facade.getDepartCities();
		
		for(String location:origins) originLocations.addElement(location);
		
		jLabelOrigin.setBounds(new Rectangle(6, 56, 92, 20));
		jLabelDestination.setBounds(6, 81, 61, 16);
		getContentPane().add(jLabelOrigin);

		getContentPane().add(jLabelDestination);

		jComboBoxOrigin.setModel(originLocations);
		jComboBoxOrigin.setBounds(new Rectangle(103, 50, 172, 20));
		

		List<String> aCities=facade.getDestinationCities((String)jComboBoxOrigin.getSelectedItem());
		for(String aciti:aCities) {
			destinationCities.addElement(aciti);
		}
		
		jComboBoxOrigin.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				destinationCities.removeAllElements();
				BLFacade facade = MainGUI.getBusinessLogic();

				List<String> aCities=facade.getDestinationCities((String)jComboBoxOrigin.getSelectedItem());
				for(String aciti:aCities) {
					destinationCities.addElement(aciti);
				}
				tableModelRides.getDataVector().removeAllElements();
				tableModelRides.fireTableDataChanged();

				
			}
		});


		jComboBoxDestination.setModel(destinationCities);
		jComboBoxDestination.setBounds(new Rectangle(103, 80, 172, 20));
		jComboBoxDestination.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {

				paintDaysWithEvents(jCalendar1,datesWithRidesCurrentMonth,	new Color(210,228,238));

				BLFacade facade = MainGUI.getBusinessLogic();

				datesWithRidesCurrentMonth=facade.getThisMonthDatesWithRides((String)jComboBoxOrigin.getSelectedItem(),(String)jComboBoxDestination.getSelectedItem(),jCalendar1.getDate());
				paintDaysWithEvents(jCalendar1,datesWithRidesCurrentMonth,Color.CYAN);

			}
		});

		this.getContentPane().add(jButtonClose, null);
		this.getContentPane().add(jComboBoxOrigin, null);

		this.getContentPane().add(jComboBoxDestination, null);


		jCalendar1.setBounds(new Rectangle(300, 50, 225, 150));


		// Code for JCalendar
		jCalendar1.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent propertychangeevent)
			{

				if (propertychangeevent.getPropertyName().equals("locale"))
				{
					jCalendar1.setLocale((Locale) propertychangeevent.getNewValue());
				}
				else if (propertychangeevent.getPropertyName().equals("calendar"))
				{
					calendarAnt = (Calendar) propertychangeevent.getOldValue();
					calendarAct = (Calendar) propertychangeevent.getNewValue();
					

					
					DateFormat dateformat1 = DateFormat.getDateInstance(1, jCalendar1.getLocale());

					int monthAnt = calendarAnt.get(Calendar.MONTH);
					int monthAct = calendarAct.get(Calendar.MONTH);

					if (monthAct!=monthAnt) {
						if (monthAct==monthAnt+2) {
							// Si en JCalendar está 30 de enero y se avanza al mes siguiente, devolvería 2 de marzo (se toma como equivalente a 30 de febrero)
							// Con este código se dejará como 1 de febrero en el JCalendar
							calendarAct.set(Calendar.MONTH, monthAnt+1);
							calendarAct.set(Calendar.DAY_OF_MONTH, 1);
						}						

						jCalendar1.setCalendar(calendarAct);

					}
					
					try {
						tableModelRides.setDataVector(null, columnNamesRides);
						tableModelRides.setColumnCount(4); // another column added to allocate ride objects

						BLFacade facade = MainGUI.getBusinessLogic();
						List<domain.Ride> rides=facade.getRides((String)jComboBoxOrigin.getSelectedItem(),(String)jComboBoxDestination.getSelectedItem(),UtilDate.trim(jCalendar1.getDate()));

						if (rides.isEmpty() ) jLabelEvents.setText(ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.NoRides")+ ": "+dateformat1.format(calendarAct.getTime()));
						else jLabelEvents.setText(ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.Rides")+ ": "+dateformat1.format(calendarAct.getTime()));
						for (domain.Ride ride:rides){
							Vector<Object> row = new Vector<Object>();
							row.add(ride.getDriver().getName());
							row.add(ride.getnPlaces());
							row.add(ride.getPrice());
							row.add(ride); // ev object added in order to obtain it with tableModelEvents.getValueAt(i,3)
							tableModelRides.addRow(row);		
						}
			            actualizarTablaConOrdenActual();

						datesWithRidesCurrentMonth=facade.getThisMonthDatesWithRides((String)jComboBoxOrigin.getSelectedItem(),(String)jComboBoxDestination.getSelectedItem(),jCalendar1.getDate());
						paintDaysWithEvents(jCalendar1,datesWithRidesCurrentMonth,Color.CYAN);


					} catch (Exception e1) {

						e1.printStackTrace();
					}
					tableRides.getColumnModel().getColumn(0).setPreferredWidth(170);
					tableRides.getColumnModel().getColumn(1).setPreferredWidth(30);
					tableRides.getColumnModel().getColumn(1).setPreferredWidth(30);
					tableRides.getColumnModel().removeColumn(tableRides.getColumnModel().getColumn(3)); // not shown in JTable

				}
			} 
			
		});

		this.getContentPane().add(jCalendar1, null);

		scrollPaneEvents.setBounds(new Rectangle(172, 257, 346, 150));

		scrollPaneEvents.setViewportView(tableRides);
		tableModelRides = new DefaultTableModel(null, columnNamesRides);

		tableRides.setModel(tableModelRides);

		tableModelRides.setDataVector(null, columnNamesRides);
		tableModelRides.setColumnCount(4); // another column added to allocate ride objects

		tableRides.getColumnModel().getColumn(0).setPreferredWidth(170);
		tableRides.getColumnModel().getColumn(1).setPreferredWidth(30);
		tableRides.getColumnModel().getColumn(1).setPreferredWidth(30);

		tableRides.getColumnModel().removeColumn(tableRides.getColumnModel().getColumn(3)); // not shown in JTable

		this.getContentPane().add(scrollPaneEvents, null);
		datesWithRidesCurrentMonth=facade.getThisMonthDatesWithRides((String)jComboBoxOrigin.getSelectedItem(),(String)jComboBoxDestination.getSelectedItem(),jCalendar1.getDate());
		paintDaysWithEvents(jCalendar1,datesWithRidesCurrentMonth,Color.CYAN);
		
		
		// IT 1
		// Crear botón para solicitar reserva
		JButton btnRequestBooking = new JButton("Request Booking");
		btnRequestBooking.setBounds(new Rectangle(354, 417, 150, 30)); // Posición debajo de la tabla
		btnRequestBooking.setEnabled(false); // deshabilitar hasta que no se seleccione un Ride
		
		// Agregar un listener a la tabla para habilitar el botón cuando se seleccione un ride
		tableRides.getSelectionModel().addListSelectionListener(event -> {
		    
	        int selectedRow = tableRides.getSelectedRow();
	        btnRequestBooking.setEnabled(selectedRow != -1); // habilitar sólo si hay una selección
	   
		});

		btnRequestBooking.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	if (loggedInUserEmail == null || loggedInUserEmail.isEmpty()) {
		            JOptionPane.showMessageDialog(null, "You must log in first!", "Error", JOptionPane.ERROR_MESSAGE);
		            return;
		        }

		        int selectedRow = tableRides.getSelectedRow();
		        if (selectedRow == -1) {
		            JOptionPane.showMessageDialog(null, "Please select a ride to request a booking.", "Error", JOptionPane.ERROR_MESSAGE);
		            return;
		        }

		        // Obtener el viaje seleccionado de la tabla
		        Ride selectedRide = (Ride) tableModelRides.getValueAt(selectedRow, 3);

		        // Abrir RequestBookingGUI con el viaje seleccionado
		        JFrame requestBookingFrame = new RequestBookingGUI(loggedInUserEmail, selectedRide);
		        requestBookingFrame.setVisible(true);
		    }
		});

		// Agregar el botón a la interfaz
		this.getContentPane().add(btnRequestBooking);
		
		buttonOrdenarPrecio = new JRadioButton("Ordenar viajes por precio");
		buttonOrdenarPrecio.setBounds(150, 206, 172, 21);
		getContentPane().add(buttonOrdenarPrecio);
		
		buttonOrdenarAsiento = new JRadioButton("Ordenar viajes por asientos");
		buttonOrdenarAsiento.setBounds(341, 206, 195, 21);
		getContentPane().add(buttonOrdenarAsiento);

		grupoOrden = new ButtonGroup();
		grupoOrden.add(buttonOrdenarPrecio);
		grupoOrden.add(buttonOrdenarAsiento);
		
		buttonOrdenarPrecio.addActionListener(e -> {
		    if (buttonOrdenarPrecio.isSelected()) {
		        ordenarTablaPorPrecio();
		    }else {
		        // Si se deselecciona, simplemente quitar el orden
		        tableModelRides.fireTableDataChanged(); // Refrescar sin ordenar
		        buttonOrdenarPrecio.setForeground(Color.BLACK);
		    }
		    
		});

		buttonOrdenarAsiento.addActionListener(e -> {
		    if (buttonOrdenarAsiento.isSelected()) {
		        ordenarTablaPorAsientos();
		    }else {
		        // Si se deselecciona, simplemente quitar el orden
		        tableModelRides.fireTableDataChanged(); // Refrescar sin ordenar
		        buttonOrdenarAsiento.setForeground(Color.BLACK);
		    }
		});
	}
	public static void paintDaysWithEvents(JCalendar jCalendar,List<Date> datesWithEventsCurrentMonth, Color color) {
		//		// For each day with events in current month, the background color for that day is changed to cyan.


		Calendar calendar = jCalendar.getCalendar();

		int month = calendar.get(Calendar.MONTH);
		int today=calendar.get(Calendar.DAY_OF_MONTH);
		int year=calendar.get(Calendar.YEAR);

		calendar.set(Calendar.DAY_OF_MONTH, 1);
		int offset = calendar.get(Calendar.DAY_OF_WEEK);

		if (Locale.getDefault().equals(new Locale("es")))
			offset += 4;
		else
			offset += 5;


		for (Date d:datesWithEventsCurrentMonth){

			calendar.setTime(d);


			// Obtain the component of the day in the panel of the DayChooser of the
			// JCalendar.
			// The component is located after the decorator buttons of "Sun", "Mon",... or
			// "Lun", "Mar"...,
			// the empty days before day 1 of month, and all the days previous to each day.
			// That number of components is calculated with "offset" and is different in
			// English and Spanish
			//			    		  Component o=(Component) jCalendar.getDayChooser().getDayPanel().getComponent(i+offset);; 
			Component o = (Component) jCalendar.getDayChooser().getDayPanel()
					.getComponent(calendar.get(Calendar.DAY_OF_MONTH) + offset);
			o.setBackground(color);
		}

		calendar.set(Calendar.DAY_OF_MONTH, today);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);


	}
	private void jButton2_actionPerformed(ActionEvent e) {
		this.setVisible(false);
	}
		
	private void ordenarTablaPorPrecio() {
	    ordenarTabla(Comparator.comparing(Ride::getPrice));
	    buttonOrdenarPrecio.setForeground(Color.BLUE); // Resaltar el botón activo
	    buttonOrdenarAsiento.setForeground(Color.BLACK);
	}

	private void ordenarTablaPorAsientos() {
	    ordenarTabla(Comparator.comparing(Ride::getnPlaces).reversed());
	    buttonOrdenarAsiento.setForeground(Color.BLUE); // Resaltar el botón activo
	    buttonOrdenarPrecio.setForeground(Color.BLACK);
	}

	private void ordenarTabla(Comparator<Ride> comparador) {
	    // Obtener los datos actuales de la tabla
	    List<Ride> rides = new ArrayList<>();
	    for (int i = 0; i < tableModelRides.getRowCount(); i++) {
	        rides.add((Ride) tableModelRides.getValueAt(i, 3));
	    }
	    
	    // Ordenar según el criterio
	    rides.sort(comparador);
	    
	    // Actualizar la tabla
	    tableModelRides.setRowCount(0);
	    for (Ride ride : rides) {
	        Vector<Object> row = new Vector<>();
	        row.add(ride.getDriver().getName());
	        row.add(ride.getnPlaces());
	        row.add(ride.getPrice());
	        row.add(ride);
	        tableModelRides.addRow(row);
	    }
	    
	    // Mantener la selección si había alguna
	    if (tableRides.getSelectedRow() != -1) {
	        int selectedRow = tableRides.getSelectedRow();
	        tableRides.setRowSelectionInterval(selectedRow, selectedRow);
	    }
	}
	private void actualizarTablaConOrdenActual() {
	    if (buttonOrdenarPrecio.isSelected()) {
	        ordenarTablaPorPrecio();
	    } else if (buttonOrdenarAsiento.isSelected()) {
	        ordenarTablaPorAsientos();
	    } else {
	        // Ningún botón seleccionado, solo refrescar
	        tableModelRides.fireTableDataChanged();
	    }
	}
}
