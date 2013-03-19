import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class Blocks {
	public static int heightTolerance = 10;
	public static double angleTolerance = Math.PI/90;
	int num;
	final int left = 250, right = Canvas.width-100, widthDelta = 80, heightDelta = 40, widthMin = 50, heightMin = 20;
	final int widthMax = widthMin + widthDelta, heightMax = heightMin + heightDelta, dis = 5;
	Point2D.Double blocksPts[][], blockPts[];
	boolean isExist[];
	int heightOfX[];
	
	public static final Color blockColor = Color.magenta;
	
	public Blocks(int n) {		
		num = n;
		blocksPts = new Point2D.Double[num][4];
		blockPts = new Point2D.Double[4];
		isExist = new boolean[num];
		heightOfX = new int[right-left];
		Random random = new Random(System.currentTimeMillis());
		int width, height;
		for(int i = left; i < right; i ++) heightOfX[i-left] = Truck.groundY;
		for(int i = 0; i < num; i ++) {
			int x = Math.abs(random.nextInt())%(right-widthMax-left-100) + left+100, yTemp = heightOfX[x-left], leftTemp = x, rightTemp = x;
			for(int j = x; j >= left; j --)
				if(heightOfX[j-left] < yTemp) break;
				else leftTemp = j;
			for(int j = x; j < right; j ++)
				if(heightOfX[j-left] < yTemp) break;
				else rightTemp = j;
			leftTemp += dis; rightTemp -= dis;
			if(rightTemp - x <= widthMin/2 || x - leftTemp <= widthMin/2) {i --; continue;}
			height = Math.abs(random.nextInt())%heightDelta + heightMin;
			width = Math.abs(random.nextInt())%(2*Math.min(Math.min(rightTemp - x, x - leftTemp), widthDelta/2) - widthMin) + widthMin;
			blocksPts[i][0] = new Point2D.Double(x-width/2, yTemp);
			blocksPts[i][1] = new Point2D.Double(x-width/2, yTemp-height);
			blocksPts[i][2] = new Point2D.Double(x+width/2, yTemp-height);
			blocksPts[i][3] = new Point2D.Double(x+width/2, yTemp);
			for(int j = x-width/2; j < x+width/2; j ++)
				heightOfX[j-left] = yTemp - height;
			isExist[i] = true;
		}
	}
	public void paint(Graphics g) {
		Graphics2D g2D = (Graphics2D)g;
		Color oldColor = g2D.getColor();
		for(int i = 0; i < num; i ++) {
			if(!isExist[i]) continue;
			Polygon block = new Polygon();
			for(int j = 0; j < 4; j ++) block.addPoint((int)blocksPts[i][j].x, (int)blocksPts[i][j].y);
			g2D.setColor(blockColor);
			g2D.fill(block);
			g2D.setColor(Color.black);
			g2D.draw(block);
		}
		g2D.setColor(oldColor);
	}
	public Point2D.Double[] getBlock(Polygon header) {
		if(header == null) return null;
		Line2D.Double line = new Line2D.Double(header.xpoints[2], header.ypoints[2], header.xpoints[1], header.ypoints[1]);
		Line2D.Double horizontal = new Line2D.Double(0, 0, 100, 0);
		double angle = getAngleBetweenLines(line, horizontal);
		if(Math.abs(angle) > Blocks.angleTolerance && Math.abs(Math.PI-angle) > Blocks.angleTolerance) return null;
		for(int i = 0; i < num; i ++) {
			if(!isExist[i]) continue;
			Line2D.Double temp[] = new Line2D.Double[4];
			for(int j = 0; j < 4; j ++)
				temp[j] = new Line2D.Double(blocksPts[i][j].x, blocksPts[i][j].y, blocksPts[i][(j+1)%4].x, blocksPts[i][(j+1)%4].y);
			for(int j = 0; j < 4; j ++) {
				if(Math.abs(blocksPts[i][j].y - blocksPts[i][(j+1)%4].y) > 10) continue;
				double dis = getDistanceBetweenLines(line, temp[j]);
				if(dis > Blocks.heightTolerance) continue;
				isExist[i] = false;
				updateHeight((int)blocksPts[i][j].x, (int)blocksPts[i][(j+1)%4].x);
				//Point2D ip = getIntersectionPointOfLines(line, temp[(j+3)%4]);
				//AffineTransform t1 = AffineTransform.getTranslateInstance(ip.getX()-temp[(j+3)%4].getX2(), ip.getY()-temp[(j+3)%4].getY2());
				for(int k = 0; k < 4; k ++) blockPts[k] = (Point2D.Double)blocksPts[i][(j+k)%4].clone();
				//t1.transform(blockPts, 0, blockPts, 0, 4);
				return blockPts;
			}
		}
		return null;
	}
	public void restore(int wellPut) {
		int i;
		for(i = 0; i < num; i ++) if(!isExist[i]) break;
		if(i == num) return;
		if(wellPut == 0)
			for(int j = 0; j < 4; j ++)
				blocksPts[i][j] = (Point2D.Double)blockPts[j].clone();
		isExist[i] = true;
	}
	
	public int[] getHeight() {
		return heightOfX;
	}
	public int getLeft() {
		return left;
	}
	public int getRight() {
		return right;
	}
	public void setHeight(int x, int h) {
		heightOfX[x-left] = h;
	}
	public void updateHeight(int l, int r) {
		if(l > r) {int t = l; l = r; r = t;}
		for(int i = l; i <= r; i ++)
			heightOfX[i-left] = 1000;
		for(int i = 0; i < num; i ++) {
			if(!isExist[i]) continue;
			double xl = 1000, xr = 0, y = 1000;
			for(int j = 0; j < 4; j ++) {	
				xl = blocksPts[i][j].getX() < xl ? blocksPts[i][j].getX() : xl;
				xr = blocksPts[i][j].getX() > xr ? blocksPts[i][j].getX() : xr;
				y = blocksPts[i][j].getY() < y ? blocksPts[i][j].getY() : y;
			}
			for(int j = Math.max((int)xl, l); j <= Math.min((int)xr, r); j ++)
				heightOfX[j-left] = Math.min(heightOfX[j-left], (int)y);
		}
	}
	
	public boolean intersect(Point2D.Double pts[], int offset, int n, Point2D.Double bPts[]) {
		for(int i = 0; i < num; i ++) {
			if(!isExist[i]) continue;
			Polygon block = getPolygonFromPoints(blocksPts[i], 0, 4);
			for(int j = offset; j < offset+n; j ++) {
				Polygon temp = getPolygonFromPoints(pts, j*4, 4);
				if(temp.intersects(block.getBounds2D()))
					return true;
			}
			if(bPts != null) {
				Polygon temp = getPolygonFromPoints(bPts, 0, 4);
				if(temp.intersects(block.getBounds2D()))
					return true;
			}
		}
		return false;
	}
	
	public static double getAngleBetweenLines(Line2D line1, Line2D line2) {
		double dy1 = line1.getY2() - line1.getY1(), dx1 = line1.getX2() - line1.getX1();
        double dy2 = line2.getY2() - line2.getY1(), dx2 = line2.getX2() - line2.getX1();
        return CraneArms.getAngleOfVectors(dx1, dx2, dy1, dy2);
	}
	public static double getDistanceBetweenLines(Line2D line1, Line2D line2) {
        double x2 = (line2.getX1() + line2.getX2())/2;
        double y2 = (line2.getY1() + line2.getY2())/2;
        return line1.ptSegDist(x2, y2);
	}
	public static Point2D getIntersectionPointOfLines(Line2D line, Line2D temp) {
		double x1 = line.getX1(), x2 = line.getX2(), x3 = temp.getX1(), x4 = temp.getX2();
		double y1 = line.getY1(), y2 = line.getY2(), y3 = temp.getY1(), y4 = temp.getY2();
		double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
		if (d == 0) return null;
		double xi = ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
		double yi = ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;
		return new Point2D.Double(xi,yi);
	}
	public static Polygon getPolygonFromPoints(Point2D.Double[] pts, int offset, int num) {
		Polygon temp = new Polygon();
		for(int i = offset; i < offset+num; i ++)
			temp.addPoint((int)pts[i].x, (int)pts[i].y);
		return temp;
	}
}
