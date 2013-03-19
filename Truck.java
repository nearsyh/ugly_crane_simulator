import java.awt.*;
import java.awt.geom.*;

public class Truck {
	final Color trackColor = Color.black, 
			wheelColor = Color.gray,
			bodyColor = Color.blue,
			groundColor = Color.green;
	public static int groundY, groundLeft, groundRight;
	final int trackWidth = 4, halfWidth = trackWidth/2;
	int body1Height = 50, body2Height = 30, body1Left, body2Left, body1Width = 110, body2Width = 30;
	RoundRectangle2D.Double track, wheel;
	Polygon body1, body2;
	BasicStroke trackStroke;
	public Truck(int x, int y) {
		groundY = y-50; groundLeft = 50; groundRight = x-50;
		int leftX = groundLeft+50, width = 2*20+body1Width, height = 50, topY = groundY-halfWidth-height, arc = 10;
		body1Left = leftX + 20; body2Left = body1Left + (body1Width-body2Width)/2;
		int body1Below = topY, body1Top = body1Below-body1Height, body2Below = body1Top, body2Top = body2Below-body2Height;
		int x1Points[] = {body1Left, body1Left, body1Left+10, body1Left+body1Width-10, body1Left+body1Width, body1Left+body1Width};
		int y1Points[] = {body1Below, body1Top+10, body1Top, body1Top, body1Top+10, body1Below};
		int x2Points[] = {body2Left, body2Left+body2Width, body2Left+body2Width/2};
		int y2Points[] = {body2Below, body2Below, body2Top};
		track = new RoundRectangle2D.Double(leftX, topY, width, height, arc, arc);
		wheel = new RoundRectangle2D.Double(leftX+trackWidth/2, topY+trackWidth/2, width-trackWidth, height-trackWidth, arc, arc);
		body1 = new Polygon(x1Points, y1Points, 6);
		body2 = new Polygon(x2Points, y2Points, 3);
		trackStroke = new BasicStroke(trackWidth);
	}
	public void paint(Graphics g) {
		Graphics2D g2D = (Graphics2D)g;
		// backup
		Color oldColor = g2D.getColor();
		BasicStroke oldStroke = (BasicStroke)g2D.getStroke();
		// paint
		g2D.setStroke(trackStroke);
		g2D.setColor(groundColor);
		g2D.drawLine(groundLeft, groundY, groundRight, groundY);
		g2D.setColor(trackColor);
		g2D.draw(track);
		g2D.setStroke(oldStroke);
		g2D.setColor(wheelColor);
		g2D.fill(wheel);
		g2D.setColor(bodyColor);
		g2D.fill(body1);
		g2D.fill(body2);
		g2D.setColor(oldColor);
	}
	public int getPivot(int i) {
		return i == 0 ? body2.xpoints[2] : (body2.ypoints[1] + body2.ypoints[2])/2;
	}
	
	public boolean intersect(Point2D.Double pts[], int offset, int num, Point2D.Double bPts[]) {
		for(int i = offset; i < offset + num; i ++) {
			Polygon temp = Blocks.getPolygonFromPoints(pts, i*4, 4);
			if(temp.intersects(wheel.getBounds2D()) || temp.intersects(body1.getBounds2D()))
				return true;
		}
		if(bPts != null) {
			Polygon temp = Blocks.getPolygonFromPoints(bPts, 0, 4);
			if(temp.intersects(wheel.getBounds2D()) || temp.intersects(body1.getBounds2D()))
				return true;
		}
		return false;
	}
}
