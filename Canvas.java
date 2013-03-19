import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.text.*;

@SuppressWarnings("serial")
public class Canvas extends JComponent {
	// Attributes
	static int width, height;
	Truck truck;
	CraneArms craneArms;
	Blocks blocks;
	JTextPane log;
	JScrollPane sp;
	BufferedImage buffer = null;
	// Status for dragging
	int dragFromX, dragFromY, dragId;
	boolean isDraging = false, isCatching = false;
	double angle;
	public Canvas(int x, int y) {
		width = x; height = y;
		angle = 0;
		setSize(x, y);
		truck = new Truck(x, y);
		craneArms = new CraneArms(4, truck.getPivot(0), truck.getPivot(1));
		blocks = new Blocks(6);
		log = new JTextPane();
		append("Welcome to the Simulator\n", 0);
		sp = new JScrollPane(log);
		log.setEditable(false);
		sp.setBounds(width-300, 10, 280, 120);
		add(sp);
		addListeners();
	}
	public void addListeners() {
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e) {
				int x = e.getX(), y = e.getY();
				if((dragId = craneArms.contain(x,y)) >= 0) {
					if(dragId == craneArms.getNum()) {
						isCatching = !isCatching;
						if(!isCatching) {
							handleDrop();
							append("The magnet is released\n", 2);
						} else
							append("The magnet is enabled\n", 2);
						repaint();
						repaint();
					} else {
						dragFromX = x; dragFromY = y;
						isDraging = true;
					}
				} else isDraging = false;
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			@SuppressWarnings("unused")
			public void mouseDragged(MouseEvent e) {
				if(isDraging) {
					if(false) return;
					else {
						double angle = craneArms.getRotateAngle(dragFromX, dragFromY, e.getX(), e.getY());
						double step = angle > 0 ? 0.001 : -0.001;
						while(Math.abs(angle)> 0.0001) {
							repaint();
							if(craneArms.rotateArm(step) > 0
									&& !truck.intersect(craneArms.getArms(), 0, craneArms.getNum()+1, craneArms.getBlock())
									&& !blocks.intersect(craneArms.getArms(), craneArms.getNum(), 1, craneArms.getBlock())) {
 								angle -= step;
								if(Math.abs(angle) < Math.abs(step)) step = angle;
							} else {
								craneArms.rotateArm(-step);
								repaint();
								break;
							}
						}
						dragFromX = e.getX();
						dragFromY = e.getY();
					}
					/*if(craneArms.catched() && craneArms.blockAngle != 0) {
						craneArms.adjust(true);
						if(blocks.intersect(null, 0, 0, craneArms.getBlock())) craneArms.adjust(false);
						else craneArms.resetAngle();
					}*/
					repaint();
				}
			}
		});
	}
	public void handleDrop() {
		int result = craneArms.dropBlock(blocks);
		blocks.restore(result);
		switch(result) {
			case 0 : append("Good Job! The block is well dropped\n", 0); break;
			case 1 : append("Be careful! The drop angle is inappropriate\n", 1); break;
			case 2 : append("Be careful! Don't drop it in the air\n", 1); break;
			case 3 : append("Be careful! It is not stable according to physici law.\n", 1); break;
		}
		if(result != 0) {
			craneArms.reset();
			append("Penalty, reset all the arms!!!", 1);
		}
	}
	public void paintComponent(Graphics g) {
		Graphics2D g2D = (Graphics2D)g;
		buffer = (BufferedImage)createImage(width, height);
		Graphics temp = buffer.createGraphics();
		truck.paint(temp);
		blocks.paint(temp);
		Polygon header = craneArms.paint(temp, isCatching);
		if(isCatching) {
			if(craneArms.catchBlock(blocks.getBlock(header))) {
				append("Catched a block!\n", 2);
			}
		}
		g2D.drawImage(buffer, 0, 0, this);
	}
	public void append(String s, int type) { 
		StyleContext sc = StyleContext.getDefaultStyleContext();
		Color temp = Color.black;
		switch(type) {
		case 0 : temp = Color.black; break;
		case 1 : temp = Color.red; break;
		case 2 : temp = Color.blue; break;
		}
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, temp);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        StyledDocument doc = log.getStyledDocument();
        try {
			doc.insertString(doc.getLength(), s, aset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
        log.setCaretPosition(doc.getLength());
	}
}
