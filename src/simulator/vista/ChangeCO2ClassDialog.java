package simulator.vista;

import simulator.control.Controller;
import simulator.factories.SetContClassEventBuilder;
import simulator.model.Event;
import simulator.model.RoadMap;
import simulator.model.TrafficSimObserver;
import simulator.model.Vehicle;
import simulator.model.exception.WrongObjectException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChangeCO2ClassDialog extends JDialog implements TrafficSimObserver{
	private Controller ctrl;
	private RoadMap map;
	private int time;
	
	public ChangeCO2ClassDialog(Controller ctrl, RoadMap map, int time, JFrame owner) {
		super(owner, true);
		this.ctrl = ctrl;
		this.map = map;
		this.time = time;
		initGUI();	
	}
	
	private void initGUI(){
		//Se crea la ventana para seleccionar los datos del evento nuevo
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		createTextArea("Schedule an event to change the CO2 class of a vehicle after a number of simulation ticks from now.");
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		//Crea ComboBox de vehicles y co2class
		centerPanel.add(new JLabel("Vehicle:"));
		JComboBox<String> listV = new JComboBox<String>();
		if(map != null) {
			if(!map.getVehicles().isEmpty()) {
				for(Vehicle v : map.getVehicles()) {
					listV.addItem(v.getId());
				}
				listV.setSelectedIndex(0);
			}
		}
		centerPanel.add(listV);
		
		centerPanel.add(new JLabel("CO2 Class:"));
		Integer nums[] = {0,1,2,3,4,5,6,7,8,9,10};
		JComboBox<Integer> listC = new JComboBox<Integer>(nums);
		centerPanel.add(listC);
		//Spinner seleccionar tiempo
		centerPanel.add(new JLabel("Ticks: "));
		JSpinner n_ticks = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		centerPanel.add(n_ticks);
		
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		bottomPanel.add(cancel);
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				try {
					if(listV.getSelectedItem() == null) {
						throw new WrongObjectException("Vehiculo no seleccionado");
					}
					else {
						//Crea JSON con la informacion del evento
						JSONObject change = new JSONObject();
						change.put("vehicle", listV.getSelectedItem());
						change.put("class", listC.getSelectedItem());
						
						JSONArray ja = new JSONArray();
						ja.put(change);
						
						JSONObject jo2 = new JSONObject();
						jo2.put("time", (int) n_ticks.getValue() + time);
						jo2.put("info", ja);
						
						JSONObject jo1 = new JSONObject();
						jo1.put("type", "set_cont_class");
						jo1.put("data", jo2);
						
						//Se añade el nuevo evento a la lista de existentes
						ctrl.addEvent(new SetContClassEventBuilder("set_cont_class").createInstance(jo1));
						dispose();
					}
				}catch(WrongObjectException ex) {
					onError(ex.getMessage());
				}
			}
		});
		bottomPanel.add(ok);
		
		mainPanel.add(bottomPanel, BorderLayout.PAGE_END);
		this.add(mainPanel);
		this.setResizable(false);
		this.setTitle("Change CO2 Class");
		this.pack();
		this.setVisible(true);
	}
	
	public void createTextArea(String msg) {
		JTextArea text = new JTextArea(msg);
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
        text.setEditable(false);
        text.setOpaque(false);
        text.setPreferredSize(new Dimension(0,40));
        this.add(text, BorderLayout.PAGE_START);
	}

	@Override
	public void onAdvanceStart(RoadMap map, List<Event> events, int time) {}

	@Override
	public void onAdvanceEnd(RoadMap map, List<Event> events, int time) {}

	@Override
	public void onEventAdded(RoadMap map, List<Event> events, Event e, int time) {}

	@Override
	public void onReset(RoadMap map, List<Event> events, int time) {}

	@Override
	public void onRegister(RoadMap map, List<Event> events, int time) {}

	@Override
	public void onError(String err) {
		JOptionPane.showMessageDialog(null, err, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
