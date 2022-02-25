package simulator.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import simulator.control.Controller;
import simulator.factories.SetWeatherEventBuilder;
import simulator.model.Event;
import simulator.model.Road;
import simulator.model.RoadMap;
import simulator.model.TrafficSimObserver;
import simulator.model.Weather;
import simulator.model.exception.WrongObjectException;

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

public class ChangeWeatherDialog extends JDialog implements TrafficSimObserver{
	private Controller ctrl;
	private RoadMap map;
	private int time;
	
	public ChangeWeatherDialog(Controller ctrl, RoadMap map, int time, JFrame owner) {
		super(owner,true);
		this.ctrl = ctrl;
		this.map = map;
		this.time = time;
        initGUI();
	}
	private void initGUI() {
		//Se crea la ventana para seleccionar los datos del evento nuevo
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		createTextArea("Schedule an event to change the weather of a road after a number of simulation ticks from now");
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		//Crea ComboBox de Road y weather
		centerPanel.add(new JLabel("Road: "));
		JComboBox<String> listR = new JComboBox<String>();
        if(map != null) {
        	if(!map.getRoads().isEmpty()) {
        		for(Road r: map.getRoads()) {
        			listR.addItem(r.getId());
        		}
			listR.setSelectedIndex(0);
        	}
        }
        centerPanel.add(listR);
        
        centerPanel.add(new JLabel("Weather: "));
        JComboBox<Weather> weather = new JComboBox<Weather>(Weather.values());
		centerPanel.add(weather);
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
		
		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(listR.getSelectedItem() == null) {
						throw new WrongObjectException("Carretera no seleccionada");
					}
					else {
						//Crea JSON con la informacion del evento
						Weather wea = (Weather) weather.getSelectedItem();
		                
		                JSONObject change = new JSONObject();
		                change.put("road", listR.getSelectedItem());	
		                change.put("weather", wea.name());
		               
		                JSONArray ja = new JSONArray();
		        		ja.put(change);
		        		
		        		JSONObject jo2 = new JSONObject();
		        		jo2.put("time", (int) n_ticks.getValue() + time);
		        		jo2.put("info", ja);
		        		
		        		JSONObject jo1 = new JSONObject();
		        		jo1.put("type", "set_weather");
		        		jo1.put("data", jo2);
		        		
		        		//Se añade el nuevo evento a la lista de existentes
		        		ctrl.addEvent(new SetWeatherEventBuilder("set_weather").createInstance(jo1));
		        		dispose();
					}
				}
				catch(WrongObjectException ex) {
					onError(ex.getMessage());
				}
				
			}
		});
		bottomPanel.add(ok);
		mainPanel.add(bottomPanel, BorderLayout.PAGE_END);
		this.add(mainPanel);
		this.setResizable(false);
		this.setTitle("Change Weather Class");
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