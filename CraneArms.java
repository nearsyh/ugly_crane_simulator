import java.awt.*;
import java.awt.geom.*;

public class CraneArms {
	final int width = 20, halfSize = width/2, height = 120, pHeight = 5, pDistance = height-2*pHeight, pRadius = 2;
	final Color armColor = Color.blue, pivotColor = Color.red;
	int num, currentId, previousId, initX, initY;
	double blockAngle, headerAngle;
	Point2D.Double pts[], pivots[], blockPts[];
	public CraneArms(int n, int initX, int initY) {
		num = n;
		pivots = new Point2D.Double[num];
		pts = new Point2D.Double[num*4+4];
		previousId = currentId = -1;
		this.initX = initX; this.initY = initY;
		blockPts = null;
		headerAngle = Math.PI;
		for(int i = 0; i < num; i ++) {
			pivots[i] = new Point2D.Double(initX, initY-pDistance*i);
			double x = pivots[i].getX(), y = pivots[i].getY();
			pts[i*4] = new Point2D.Double(x-halfSize, y+pHeight);
			pts[i*4+1] = new Point2D.Double(x-halfSize, y+pHeight-height);
			pts[i*4+2] = new Point2D.Double(x+halfSize, y+pHeight-height);
			pts[i*4+3] = new Point2D.Double(x+halfSize, y+pHeight);
		}
		double temp = pts[num*4-3].getY();
		pts[num*4] = new Point2D.Double(initX-height/4, temp);
		pts[num*4+1] = new Point2D.Double(initX-height/4, temp-width*2/3);
		pts[num*4+2] = new Point2D.Double(initX+height/4, temp-width*2/3);
		pts[num*4+3] = new Point2D.Double(initX+height/4, temp);
	}
	public double rotateArm(double angle) {
		AffineTransform rotate = AffineTransform.getRotateInstance(angle, pivots[currentId].getX(), pivots[currentId].getY());
		rotate.transform(pts, currentId*4, pts, currentId*4, (num-currentId+1)*4);
		if(blockPts != null) rotate.transform(blockPts, 0, blockPts, 0, 4);
		boolean outbound = false;
		for(int i = 0; i < num*4+4; i ++)
			outbound |= pts[i].x < 0 || pts[i].y < 0 || pts[i].y > Truck.groundY;
		if(blockPts != null)
			for(int i = 0; i < 4; i ++)
				outbound |= blockPts[i].x < 0 || blockPts[i].y < 0 || blockPts[i].y > Truck.groundY;
		if(outbound) {
			rotate = AffineTransform.getRotateInstance(-angle, pivots[currentId].getX(), pivots[currentId].getY());
			rotate.transform(pts, currentId*4, pts, currentId*4, (num-currentId+1)*4);
			if(blockPts != null) rotate.transform(blockPts, 0, blockPts, 0, 4);
			return -1;
		}
		rotate.transform(pivots, currentId, pivots, currentId, num-currentId);
		headerAngle -= angle;
		if(headerAngle < 0) headerAngle += Math.PI*2;
		else if(headerAngle > Math.PI*2) headerAngle -= Math.PI*2;
		return Math.abs(angle);
	}
	public int contain(int x, int y) {
		int i;
		for(i = num; i >= 0; i --) {
			Polygon arm = new Polygon();
			for(int j = 0; j < 4; j ++)
				arm.addPoint((int)pts[i*4+j].getX(), (int)pts[i*4+j].getY());
			if(arm.contains(x, y)) break;
		}
		previousId = currentId;
		currentId = Math.min(i, num-1);
		return i;
	}
	public boolean catchBlock(Point2D.Double b[]) {
		if(b == null || blockPts != null) return false;
		currentId = num-1;
		rotateArm(headerAngle);
		//double dx = pts[num*4+1].getX()-pts[num*4+2].getX(), dy = pts[num*4+1].getY()-pts[num*4+2].getY();
		//blockAngle = CraneArms.getAngleOfVectors(10, dx, 0, dy);
		double h = b[3].getY() - b[0].getY();
		b[0].y = pts[num*4+1].getY();
		b[1].y = pts[num*4+1].getY();
		b[2].y = b[0].y + h;
		b[3].y = b[0].y + h;
		blockPts = b;
		return true;
	}
	public int dropBlock(Blocks blocks) {
		if(blockPts == null) return -1;
		int isWellPut = 0;
		double angle;
		for(int i = 0; i < 4; i ++) {
			angle = getAngleOfVectors(10, blockPts[i].x-blockPts[(i+1)%4].x, 0, blockPts[i].y-blockPts[(i+1)%4].y);
			if(Math.abs(angle) > Blocks.angleTolerance && Math.abs(Math.PI-angle) > Blocks.angleTolerance) {
				if(isWellPut < 1) isWellPut = 1;
				continue;
			}
			if(Math.abs(headerAngle) < Blocks.angleTolerance) rotateArm(headerAngle);
			else if(Math.abs(headerAngle-Math.PI/2) < Blocks.angleTolerance) rotateArm(headerAngle-Math.PI/2);
			else rotateArm(headerAngle-Math.PI*3/2);
			int bX1 = (int)blockPts[i].x, bX2 = (int)blockPts[(i+1)%4].x, bY1 = (int)blockPts[i].y, bY2 = (int)blockPts[(i+1)%4].y;
			int left = blocks.getLeft(), height[] = blocks.getHeight(), mid = (bX1 + bX2)/2, h = bY1 - (int)blockPts[(i+3)%4].y;
			if(h < 0) continue;
			if(bX1 > bX2) {
				int temp = bX1; bX1 = bX2; bX2 = temp;
				temp = bY1; bY1 = bY2; bY2 = temp;
			}
			int y1 = 1000, y2 = y1;
			for(int j = bX1; j < mid; j ++)
				y1 = y1 < height[j-left] ? y1 : height[j-left];
			for(int j = mid; j <= bX2; j ++)
				y2 = y2 < height[j-left] ? y2 : height[j-left];
			if(Math.abs(y1 - bY1) > Blocks.heightTolerance && Math.abs(y2 - bY1) > Blocks.heightTolerance) {
				if(isWellPut < 2) isWellPut = 2;
				continue;
			}
			if(y1 != y2) {
				isWellPut = 3;
				continue;
			}
			blockPts[i].setLocation(bX1, y1);
			blockPts[(i+1)%4].setLocation(bX1, y1-h);
			blockPts[(i+2)%4].setLocation(bX2, y1-h);
			blockPts[(i+3)%4].setLocation(bX2, y1);
			for(int j = bX1; j <= bX2; j ++)
				blocks.setHeight(j, y1-h);
			isWellPut = 0;
			break;
		}
		blockPts = null;
		return isWellPut;
	}
	public Polygon paint(Graphics g, boolean isCatching) {
		Graphics2D g2D = (Graphics2D)g;
		Color oldColor = g2D.getColor();
		Polygon toReturn = new Polygon();
		for(int i = 0; i < num+1; i ++) {
			Polygon arm = new Polygon();
			for(int j = 0; j < 4; j ++)
				arm.addPoint((int)pts[i*4+j].getX(), (int)pts[i*4+j].getY());
			g2D.setColor(armColor);
			if(i == num && isCatching) g2D.setColor(Color.black);
			g2D.fill(arm);
			g2D.setColor(Color.black);
			g2D.draw(arm);
			if(i < 4) {
				g2D.setColor(pivotColor);
				int x = (int)pivots[i].getX()-pRadius, y = (int)pivots[i].getY()-pRadius;
				g2D.fillArc(x, y, 2*pRadius, 2*pRadius, 0, 360);
				g2D.setColor(Color.black);
				g2D.drawArc(x, y, 2*pRadius, 2*pRadius, 0, 360);
			}
			toReturn = arm;
		}
		if(blockPts != null) {
			Polygon block = new Polygon();
			for(int j = 0; j < 4; j ++)
				block.addPoint((int)blockPts[j].getX(), (int)blockPts[j].getY());
			g2D.setColor(Blocks.blockColor);
			g2D.fill(block);
			g2D.setColor(Color.black);
			g2D.draw(block);
		}
		g2D.setColor(oldColor);
		return blockPts == null ? toReturn : null;
	}
	public void adjust(boolean first) {
		double angle = first ? blockAngle : -blockAngle;
		AffineTransform rotate = AffineTransform.getRotateInstance(angle, blockPts[0].getX(), blockPts[0].getY());
		rotate.transform(blockPts, 0, blockPts, 0, 4);
	}
	public void resetAngle() {
		blockAngle = 0;
	}
	public void reset() {
		blockPts = null;
		headerAngle = Math.PI;
		for(int i = 0; i < num; i ++) {
			pivots[i] = new Point2D.Double(initX, initY-pDistance*i);
			double x = pivots[i].getX(), y = pivots[i].getY();
			pts[i*4] = new Point2D.Double(x-halfSize, y+pHeight);
			pts[i*4+1] = new Point2D.Double(x-halfSize, y+pHeight-height);
			pts[i*4+2] = new Point2D.Double(x+halfSize, y+pHeight-height);
			pts[i*4+3] = new Point2D.Double(x+halfSize, y+pHeight);
		}
		double temp = pts[num*4-3].getY();
		pts[num*4] = new Point2D.Double(initX-height/4, temp);
		pts[num*4+1] = new Point2D.Double(initX-height/4, temp-width*2/3);
		pts[num*4+2] = new Point2D.Double(initX+height/4, temp-width*2/3);
		pts[num*4+3] = new Point2D.Double(initX+height/4, temp);
	}
	
	public int getNum() {
		return num;
	}
	public boolean catched() {
		return blockPts != null;
	}
	public Point2D.Double[] getArms() {
		return pts;
	}
	public Point2D.Double[] getBlock() {
		return blockPts;
	}
	public double getRotateAngle(int startX, int startY, int endX, int endY) {
		double dx1 = startX-pivots[currentId].getX(), dx2 = endX-pivots[currentId].getX();
		double dy1 = startY-pivots[currentId].getY(), dy2 = endY-pivots[currentId].getY();
		double angle = getAngleOfVectors(dx1, dx2, dy1, dy2);
		return angle;
	}
	public static double getAngleOfVectors(double dx1, double dx2, double dy1, double dy2) {
		double len1 = Math.sqrt(dx1*dx1 + dy1*dy1), len2 = Math.sqrt(dx2*dx2 + dy2*dy2);
		double angle = Math.acos((dx1*dx2 + dy1*dy2)/(len1 * len2));
		if(dx1 * dy2 - dy1 * dx2 < 0) angle = -angle;
		return angle;
	}
}
