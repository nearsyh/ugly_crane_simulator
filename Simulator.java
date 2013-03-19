import java.awt.*;
import java.awt.event.*;

import javax.swing.JFrame;

// The main window, do nothing, just display the canvas
@SuppressWarnings("serial")
public class Simulator extends JFrame{
	Canvas canvas;
	public Simulator() {
		super("CraneSimulator");
		setSize(800, 650);
		setLayout(new BorderLayout());
		
		canvas = new Canvas(800, 650);
		canvas.setVisible(true);
		add(canvas, BorderLayout.CENTER);
		addListeners();
	}
	public void addListeners() {
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
	}
	public static void main(String[] args) {
		Simulator simulator = new Simulator();
		simulator.setResizable(false);
		simulator.setVisible(true);
	}
}
