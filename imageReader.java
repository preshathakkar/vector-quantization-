import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

public class imageReader {

  
   public static void main(String[] args) 
   {
   	
	
	String fileName = args[0];
	int N = Integer.parseInt(args[1]);
   	int width = 352;
	int height = 288;
	int[][] r = new int[width][height];
	byte[][] rnew = new byte[width][height];
	Point[][] pixpt = new Point[width/2][height]; 
	ClusterPoint[] cpt = new ClusterPoint[N];	

    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    BufferedImage img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    try {
	    File file = new File(args[0]);
	    InputStream is = new FileInputStream(file);

	    long len = file.length();
	    byte[] bytes = new byte[(int)len];
	    
	    int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
    		
    	int ind = 0;
		for(int h = 0; h < height; h++){
	
			for(int w = 0; w < width; w++){
		 //System.out.println("h:"+h+"\tw:"+w+"\n");
				byte a = 0;
				byte red = bytes[ind];
				r[w][h] = red&0xff;
				//byte g = bytes[ind+height*width];
				//byte b = bytes[ind+height*width*2]; 
				
				int pix = 0xff000000 | ((r[w][h] & 0xff) << 16) | ((r[w][h] & 0xff) << 8) | (r[w][h] & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);

				img.setRGB(w,h,pix);
				ind++;
			}
			//System.out.println("h:"+h+"\n");
		}
	
	//int max=0,min=255;
	//for(int h = 0; h < height; h++){
	//
	//		for(int w = 0; w < width; w++){
	//			 max=Math.max(max,r[w][h]);
	//			 min=Math.min(min,r[w][h]);
	//
	//		}	
	//}
	//System.out.println("Max : "+max+"\tMin : "+min);

	InitializeK(cpt);
	InitializePoints(pixpt,r);
	for(int x=0;x<20000;x++){
		for(int w=0;w<pixpt.length;w++){
			for(int h=0;h<pixpt[w].length;h++){
				AddPointInCluster(pixpt[w][h],cpt);
				
			}
		}
		Point[] check=new Point[cpt.length];
		for(int n=0;n<cpt.length;n++){
			check[n]=new Point();
			check[n].setLocation(cpt[n].k);
		}
		UpdateCluster(cpt);
		if(ClusterCheck(cpt,check)){
			//for(int t=0;t<cpt.length;t++){
			//System.out.println(cpt[t].k.toString());}
			//System.out.println("Same Points" +x);
			break;
		}	
	}
	
	for(int w=0;w<pixpt.length;w++){
		for(int h=0;h<pixpt[w].length;h++){
			Point close = FindClusterPoint(pixpt[w][h],cpt);
			pixpt[w][h].setLocation(close);
		}
	}
		

	for(int w=0;w<pixpt.length;w++){
		for(int h=0;h<pixpt[w].length;h++){
			int w2 = (w*2) +1;
			int w1 = (w*2);
			rnew[w1][h]=(byte)pixpt[w][h].getX();
			rnew[w2][h]=(byte)pixpt[w][h].getY();

		//			System.out.println("w : "+w+"\th : "+h+"\tw1 : "+w1+"\tw2 : "+w2);
		}
	}
	
	ind =0;
	for(int h = 0; h < height; h++){
	
			for(int w = 0; w < width; w++){
//if(rnew[w][h]>126||rnew[w][h]<-126){
//					System.out.println(rnew[w][h]+"\t");
//				}
				if(rnew[w][h]<=-127){
					rnew[w][h]=(byte)-127;
				}
				else if(rnew[w][h]>=127){
					rnew[w][h]=(byte)127;
				}

				
		 //System.out.println("h:"+h+"\tw:"+w+"\n");
				//byte g = bytes[ind+height*width];
				//byte b = bytes[ind+height*width*2]; 
				
				int pix = 0xff000000 | ((rnew[w][h] & 0xff) << 16) | ((rnew[w][h] & 0xff) << 8) | (rnew[w][h] & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);

				img2.setRGB(w,h,pix);
				ind++;
			}
			//System.out.println("h:"+h+"\n");
		}
		
		
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
	e.printStackTrace();	
	}
	
    
    // Use a panel and label to display the image
    JPanel  panel = new JPanel ();
    panel.add (new JLabel (new ImageIcon (img)));
    panel.add (new JLabel (new ImageIcon (img2)));
    
    JFrame frame = new JFrame("Display images");
    
    frame.getContentPane().add (panel);
    frame.pack();
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   

   }

	public static void InitializeK(ClusterPoint[] cp){
		int x,y;
		int ind=0, change=cp.length;

		if(cp.length>6){
		int initializeNum = (int)Math.floor(Math.sqrt(cp.length));
		int basic = (int)Math.pow(initializeNum,2);
		int stepbasic = (int)256/initializeNum;
		int startbasic = (int)stepbasic/2;
		//System.out.println("In w : iniNum : "+initializeNum+"\tbasic : "+basic+"\n");
		//System.out.println("In w : stepbasic : "+stepbasic+"\tstartbasic : "+startbasic+"\n");
	
		for(int h=0,yp=startbasic;h<initializeNum;h++){
			for(int w=0,xp=startbasic;w<initializeNum;w++){
				//System.out.println("In w ind : "+ind+"\n");
				cp[ind] = new ClusterPoint();
				cp[ind].k = new Point(xp,yp);
				xp+=stepbasic;
				ind++;
			
			}
			yp+=stepbasic;
		}
			change -= basic;
		}		
		
		Random rand = new Random();
		//System.out.println("Out w ind : "+ind+"\tchange : "+change+"\n");
	
		//System.out.println(cp.length);
		for(int temp=0;temp<change;temp++,ind++){
		//	System.out.println("ind = "+ind+"\t");
			x = rand.nextInt(255);
			y = rand.nextInt(255);
//			System.out.println("hey");
			 
			cp[ind] = new ClusterPoint();
//			System.out.println("hello");
			
			cp[ind].k = new Point();
			cp[ind].k.setLocation(x,y);
			//System.out.println("In change ind : "+ind+"\n");
	
		//	System.out.println("("+x+","+y+")\t");
		//	System.out.println("("+cp[ind].k.getX()+","+cp[ind].k.getY()+")\n");
		//for(int n=0;n<ind;n++){
		//System.out.println("Pt["+n+"] : ("+cp[n].k.getX()+","+cp[n].k.getY()+")\n");}
		
		}
		//for(int n=0;n<cp.length;n++){
		//System.out.println("Pt["+n+"] : ("+cp[n].k.getX()+","+cp[n].k.getY()+")\n");}

	}

	public static void InitializePoints(Point[][] p, int[][] r){
		for(int h=0;h<p[0].length;h++){
			for(int w=0;w<p.length;w++){
				int w2 = (w*2) +1;
				int w1 = (w*2);
				p[w][h] = new Point(r[w1][h],r[w2][h]);
				//if(r[w1][h]!=r[w2][h]){
//System.out.println("Point["+w+"]["+h+"] : "+p[w][h].toString()+" r["+w1+"]["+h+"] : "+r[w1][h]+" r["+w2+"]["+h+"] : "+r[w2][h]+"\n");}	
			}
		}

		

		
	
	}	

	public static Point FindClusterPoint(Point pt, ClusterPoint[] cp){
		double minDist=255;
		double minDistNew=0;
//		System.out.println("Point : "+pt.toString()+"\n");		
		Point c = new Point();
		for(int ind=0;ind<cp.length;ind++){
			double dist = Math.abs(pt.distance(cp[ind].k));
			//System.out.println("Distance : "+dist+"\t");
			minDistNew=Math.min(minDist,dist);
			//System.out.println("minDist : "+minDist+"\t");
			//System.out.println("Minimum : "+minDistNew+"\n");
			
			if(minDistNew<minDist){
				minDist=minDistNew;
				//System.out.println("hey.. am in");
				c.setLocation(cp[ind].k);
			}
		}	
		return c;
	}

	public static void AddPointInCluster(Point pt, ClusterPoint[] cp){
//		System.out.println("Point : "+pt.toString()+"\t");		
		Point c = FindClusterPoint(pt,cp);
//		System.out.println("CL Point : "+c.toString()+"\n");		
		
		for(int ind=0;ind<cp.length;ind++){
			//System.out.println("how's you babe");
			
			if(cp[ind].k.equals(c)){
				//System.out.println(cp[ind].countk);
				Point ptemp = new Point(pt);
				cp[ind].in.add(cp[ind].countk,ptemp);
				//cp[ind].in[cp[ind].countk]= new Point();
				//cp[ind].in[cp[ind].countk].setLocation(pt);				
				cp[ind].countk++;
//				System.out.println("heeyyyyyy");
			//	System.out.println("Countk : "+cp[ind].countk+"\n");
			}	
		}
	}

	public static void UpdateCluster(ClusterPoint[] cp){
		for(int ind=0;ind<cp.length;ind++){
			int x=0,y=0;
			int count;
			Random rand = new Random();
			//System.out.println(cp[ind].countk);
				
			for(count=0;count<cp[ind].countk;count++){
				x+=cp[ind].in.get(count).getX();
				y+=cp[ind].in.get(count).getY();
			}
			if(count!=0){		
			x/=count;
			y/=count;}
			//if(Math.abs(x)<=2){
			//	x=4;
			//}
			//if(Math.abs(y)<=2){
			//	y=4;
			//}
			
			cp[ind].k.setLocation(x,y); 
		}
	}

	public static boolean ClusterCheck(ClusterPoint[] cp, Point[] p){
		boolean flag = true;
		for(int ind=0;ind<cp.length;ind++){
			if(!cp[ind].k.equals(p[ind])){
				//double diffX = cp[ind].k.getX()-p[ind].getX();
				//double diffY = cp[ind].k.getY()-p[ind].getY();
				//if(diffX==0 && diffY==0 && flag==true){
				//	flag = true;
				//}
				//else{
					flag = false;
				//}
			}
		}
		return flag;
	}
	
	public static class ClusterPoint {
		public Point k;
		public int countk;
		public java.util.List<Point> in;	
		
		ClusterPoint(){
			countk = 0;
			in = new ArrayList<Point>();
		}	

		ClusterPoint(int x, int y){
			k = new Point(x,y);
			countk = 0;
			in = new ArrayList<Point>();

		}

	}

  
}
