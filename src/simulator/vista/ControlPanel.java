package simulator.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import simulator.control.Controller;
import simulator.model.Event;
import simulator.model.RoadMap;
import simulator.model.TrafficSimObserver;

public class ControlPanel extends JPanel implements TrafficSimObserver{
	
	private Controller ctrl;
	
	private RoadMap map;
	private int time;
	private JFrame owner;
	
	boolean _stopped = false;
	JButton b_events, b_contClass, b_setWeather, b_run, b_exit;
	File _inFile;
	JSpinner n_ticks;
	
	public ControlPanel(Controller ctrl, JFrame owner) {
		this.ctrl = ctrl;
		this.owner = owner;
		ctrl.addObserver(this);
		initGUI();
	}
	
	private void initGUI() {
		this.setLayout(new BorderLayout());
		
		//barra con los diferentes botones
		JToolBar toolBar = crearMainToolBar();
		this.add(toolBar, BorderLayout.WEST);		
		
		JToolBar toolBar2 = crearToolBar();
		
		//barra del exit
		b_exit = crearBoton(new ImageIcon("resources/icons/exit.png"), "Exit the simulator");
		b_exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(JOptionPane.showOptionDialog(null, "Are you sure you want to exit?", "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == JOptionPane.YES_OPTION) System.exit(0);
			}
		});
		toolBar2.add(b_exit);
		
		this.add(toolBar2, BorderLayout.EAST);
	}
	
	
	private JToolBar crearMainToolBar() {
		JToolBar toolBar = crearToolBar();
		
		b_events = crearBoton(new ImageIcon("resources/icons/open.png"), "Open a new file");
		b_events.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				load_events();
			}
		});
		toolBar.add(b_events);
		
		toolBar.addSeparator();
		
		b_contClass = crearBoton(new ImageIcon("resources/icons/co2class.png"), "Change CO2 Class of a Vehicle");
		b_contClass.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ChangeCO2ClassDialog(ctrl, map, time, owner);
			}
		});
		toolBar.add(b_contClass);
		
		b_setWeather = crearBoton(new ImageIcon("resources/icons/weather.png"), "Change Weather of a Road");
		b_setWeather.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ChangeWeatherDialog(ctrl, map, time, owner);
			}
		});
		toolBar.add(b_setWeather);
		
		toolBar.addSeparator();
		
		b_run = crearBoton(new ImageIcon("resources/icons/run.png"), "Run the simulator");
		b_run.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				run();
			}
		});
		toolBar.add(b_run);
		
		JButton b_stop = crearBoton(new ImageIcon("resources/icons/stop.png"), "Stop the simulator");
		b_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setStop(true);
			}
		});
		toolBar.add(b_stop);
		//Spinner con los ticks que avanzar al pulsar play
		toolBar.add(new JLabel("Ticks: "));
		n_ticks = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
		n_ticks.setMaximumSize(new Dimension(75,25));
		n_ticks.setPreferredSize(new Dimension(75,25));
		n_ticks.setToolTipText("Simulation tick to run: 1-10000");
		toolBar.add(n_ticks);
		
		return toolBar;
	}
	
	private JToolBar crearToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		return toolBar;
	}
	
	private JButton crearBoton(ImageIcon i, String toolTip) {
		JButton boton = new JButton();
		boton.setIcon(i);
		boton.setToolTipText(toolTip);
		return boton;
	}
	
	private void load_events(){
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("resources/examples"));
		if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			_inFile = fc.getSelectedFile();
			InputStream in;
			try {
				in = new FileInputStream(_inFile);
				ctrl.reset();
				ctrl.loadEvents(in);
			} catch (Exception ex) {
				onError(ex.getMessage());
			}
		}
	}
	void run() {
		setStop(false);
		enableToolBar(false);
		run_sim((int) n_ticks.getValue());
	}
	
	private void run_sim(int n) {
		if (n > 0 && !_stopped) {
			try {
				ctrl.run(1, null);
			} catch (Exception e) {
				onError(e.getMessage());
				setStop(true);
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					run_sim(n - 1);
				}
			});
		}
		else {
			//si el run esta activado, algunos botones quedan inhabilitados
			enableToolBar(true);
			setStop(true);
		}
	}
	
	private void enableToolBar(boolean enable) {
		b_events.setEnabled(enable);
		b_contClass.setEnabled(enable);
		b_setWeather.setEnabled(enable);
		b_run.setEnabled(enable);
		b_exit.setEnabled(enable);
	}
	
	void setStop(boolean stop) {
		_stopped = stop;
	}
	
	@Override
	public void onAdvanceStart(RoadMap map, List<Event> events, int time) {
		this.map = map;
		this.time = time;
	}

	@Override
	public void onAdvanceEnd(RoadMap map, List<Event> events, int time) {}

	@Override
	public void onEventAdded(RoadMap map, List<Event> events, Event e, int time) {	}

	@Override
	public void onReset(RoadMap map, List<Event> events, int time) {}

	@Override
	public void onRegister(RoadMap map, List<Event> events, int time) {}

	@Override
	public void onError(String err) {
		JOptionPane.showMessageDialog(null, err, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
