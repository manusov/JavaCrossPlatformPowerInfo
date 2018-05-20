//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Icon class for Charging Monitor drawings y=f(x) ===================

package powerinfo;

import javax.swing.*;
import java.awt.*;
import static java.lang.Math.abs;

public class GraphIcon implements Icon {

//--- Constants and variables ---
private final static int XB1=60, YB1=37-4, XB2=100, YB2=10+4;
private final static int W_GRID=10, H_GRID=6;
private int width=0, height=0;
private int x1=0, y1=0, x2=0, y2=0, dx=0, dy=0, xa=0;
private double minX, maxX, minY, maxY;
private double x,y,sx,sy;
private double maximum=Double.NaN, value=Double.NaN, minimum=Double.NaN;
private double yext=0.0;  // added for v0.52

//--- Drawings data ---
private int drawX;             // X-length, size of used part in the array drawY
private final double[] drawY;  // Array of Y-values
private int xzero, yzero, xdraw1, ydraw1, xdraw2, ydraw2;

//--- Colors ---
private final Color bclr = new Color ( 245,245,245 );   // background color
private final Color gclr = new Color ( 200,200,200 );   // grid color
private final Color ptxt = new Color ( 10,10,120 );     // parameter names
private final Color vtxt = new Color ( 120,10,10 );     // min-max values
private final Color gtxt = new Color ( 10,10,10 );      // grid labels
private final Color cdrw = new Color ( 10,10,250);      // drawings
private final Color etxt = new Color ( 160,150,150);    // unavaliable text

//--- Fonts ---
private final Font  pfnt = new Font  ( "Verdana", Font.BOLD, 11 );  // x,y names
private final Font  vfnt = new Font  ( "Verdana", Font.BOLD, 10  ); // min-max
private final Font  gfnt = new Font  ( "Verdana", Font.PLAIN, 11 ); // grid num.

//--- Strings ---
private String nameX, nameY, s;
private final String nameMax   = "max ";
private final String nameValue = "value ";
private final String nameMin   = "min ";

//--- Addition for secondary model ---
private int drawXsecond;             // X-length, size of used part array drawY
private final double[] drawYsecond;                        // Array of Y-values
private double valueSecond = Double.NaN;                   // Actual value
private final Color cdrwSecond = new Color ( 10,105,10);   // drawings


//--- Icon constructor ---
public GraphIcon(int w, int h)
    {
    width=w;
    height=h;
    drawX=0;
    drawXsecond=0;
    int n = width-XB1-XB2;
    drawY = new double[n];
    drawYsecond = new double[n];
    for (int i=0; i<n; i++) 
        { 
        drawY[i]=0.0;
        drawYsecond[i]=0.0;
        }
    }

//--- Standard method: get icon width ---
@Override public int getIconWidth()  { return width; }

//--- Standard method: get icon height ---
@Override public int getIconHeight() { return height; }

//--- Standard method: paint icon ---
@Override public void paintIcon(Component c, Graphics g, int w, int h )
    {
    //--- Background ---
    g.setColor(bclr);
    int xmax=w+width-1;
    int ymax=h+height-1;
    g.fillRect(0, 0, xmax, ymax);
    
    //--- Label, name of X-parameter ---
    g.setColor(ptxt);
    g.setFont(pfnt);
    FontMetrics fm = g.getFontMetrics();
    xa = fm.stringWidth(nameX);
    x1 = w + width/2 - xa/2;
    y1 = h + height - 10;
    g.drawString(nameX, x1, y1);
    x1 = w + XB1;
    y1 = h + 11;
    g.drawString(nameY, x1, y1);
    
    //--- Labels max/current/min ---
    g.setFont(vfnt);
    
    //--- maximum value ---
    g.setColor(vtxt);
    s = nameMax;
    if (!Double.isNaN(maximum)) { s = s + String.format("%.1f",maximum); }
    if ((Double.isNaN(maximum))&(drawX>0)) { g.setColor(etxt); }
    x1=w+width-XB2+12;
    y1=h+YB2+4;
    g.drawString(s, x1, y1+10);
    
    //--- current value, bat #0 --- 
    g.setColor(cdrw);    // (vtxt);
    s = nameValue;
    if (!Double.isNaN(value)) 
        { s = s + String.format("%.1f",value); }
    if ((Double.isNaN(value))&(drawX>0))
        { s = "unreadable"; g.setColor(etxt); }
    y1+=(height-YB1-YB2)/2;
    g.drawString(s, x1, y1);

    //--- current value, bat #1 --- 
    g.setColor(cdrwSecond);    // (vtxt);
    s = nameValue;
    if (!Double.isNaN(valueSecond)) 
        { s = s + String.format("%.1f",valueSecond); }
    if ((Double.isNaN(valueSecond))&(drawXsecond>0))
        { s = "unreadable"; g.setColor(etxt); }
    y1 += 12;
    g.drawString(s, x1, y1);
    y1 -= 12;
    
    //--- minimum value ---
    g.setColor(vtxt);
    s = nameMin;
    if (!Double.isNaN(minimum)) { s = s + String.format("%.1f",minimum); }
    if ((Double.isNaN(minimum))&(drawX>0)) { g.setColor(etxt); }
    y1+=(height-YB1-YB2)/2;
    g.drawString(s, x1, y1-10);

    //--- X-grid, horizontal sequence of vertical lines ---
    g.setColor(gclr);
    x1=w+XB1; y1=height-YB1; x2=x1; y2=YB2;
    dx=(width-XB1-XB2)/W_GRID;
    for (int i=0; i<=W_GRID; i++) {g.drawLine(x1, y1, x2, y2); x1+=dx; x2+=dx;}
    
    //--- Y-grid, vertical sequence of horizontal lines ---
    x1=w+XB1; y1=height-YB1; x2=width-XB2; y2=y1;
    dy=(height-YB1-YB2)/H_GRID;
    for (int i=0; i<=H_GRID; i++) {g.drawLine(x1, y1, x2, y2); y1-=dy; y2-=dy;}
    
    //--- Text labels for X-grid ---
    g.setColor(gtxt);
    g.setFont(gfnt);
    x1=w+XB1-3; y1=h+height-YB1+13;
    x=minX;
    sx=(maxX-minX)/W_GRID;
    for (int i=0; i<=W_GRID; i++)
        {
        String s = String.format("%d",(int)x);
        g.drawString(s, x1, y1);
        x1+=dx; x+=sx;
        }
    
    //--- Text labels for Y-grid ---
    fm = g.getFontMetrics();
    x1=w+XB1-2; y1=h+height-YB1+5;
    y=minY;
    sy=(maxY-minY)/H_GRID;
    for (int i=0; i<=H_GRID; i++)
        {
        s = String.format("%d",(int)y);
        xa = x1 - fm.stringWidth(s);
        g.drawString(s, xa, y1);
        y1-=dy; y+=sy;
        }

    //--- Function Value=F(Time) drawing ---
    int drawXboth = 0;
    if ( drawX > 1      ) { drawXboth = drawX; }
    if (drawXsecond > 1 ) { drawXboth = drawXsecond; }
    
    if ( drawXboth > 1 )
        {
        xzero = XB1;
        yzero = height - YB1;
        
        for (int i=1; i<drawXboth; i++)
            {
            //--- Set x1, x2 ---
            xdraw1 = xzero + i;
            xdraw2 = xdraw1 + 1;
            //--- Detect value per pixel ---
            double pixelrange    = height - YB1 - YB2;
            double valuerange    = maxY - minY;
            double valueperpixel = valuerange / pixelrange;
            double pixelpervalue = pixelrange / valuerange;
            double correction = minY * pixelpervalue;

        double a=0.0, b=0.0;    
        if (drawX>1)
            {
            //--- Get previous and current point for drawings ---
            a = drawY[i-1];
            b = drawY[i];
            //--- Drawings only if parameter valid ---
            if ( !( (Double.isNaN(a))|(Double.isNaN(b)) ) )
                {
                //--- Detect Y-coordinates for situation minY-based y-axis ---
                ydraw1 = yzero - (int)
                    ( Math.round( a / valueperpixel - correction ) );
                ydraw2 = yzero - (int)
                    ( Math.round( b / valueperpixel - correction ) );
                //--- Draw ---
                g.setColor(cdrw);
                g.drawLine( xdraw1, ydraw1, xdraw2, ydraw2 );
                }
            }
        
        if (drawXsecond>1)
            {    
            //--- Get previous and current point for drawings ---
            a = drawYsecond[i-1];
            b = drawYsecond[i];
            //--- Drawings only if parameter valid ---
            if ( !( (Double.isNaN(a))|(Double.isNaN(b)) ) )
                {
                //--- Detect Y-coordinates for situation minY-based y-axis ---
                ydraw1 = yzero - (int)
                    ( Math.round( a / valueperpixel - correction ) );
                ydraw2 = yzero - (int)
                    ( Math.round( b / valueperpixel - correction ) );
                //--- Draw ---
                g.setColor(cdrwSecond);
                g.drawLine( xdraw1, ydraw1, xdraw2, ydraw2 );
                }
            }
            }
        }
    }

//--- Child-specific method: clear measurement ---
public void clearMeasure()
    { 
    maximum=Double.NaN;
    value=Double.NaN;
    minimum=Double.NaN;
    drawX=0;
    
    drawXsecond = 0;
    valueSecond = Double.NaN;
    }

//--- Child-specific method: get actual used X-width for drawings ---
public int getUsedWidth() { return width-XB1-XB2; }

//--- Child-specific methods: set names for X,Y-axis ---
public void setNameX(String s) { nameX = s; }
public void setNameY(String s) { nameY = s; }

//--- Child-specific methods: set drawings X,Y-limits ---
public void setMinX(double a) { minX = a; }
public void setMaxX(double a) { maxX = a; }
public void setMinY(double a) { minY = a; }
public void setMaxY(double a) { maxY = a; }

//--- Child-specific method: set data for drawings ---

public void setDrawData1(int x, double[] y)  
    {
    //--- Set array(Y) and array size(X) ---
    drawX=x;                                     // set X-size of used part 
    for (int i=0; i<x; i++) { drawY[i]=y[i]; }   // set array of Y-values
    
    //--- Detect current/minimum/maximum values ---
    value=y[x-1];
    minimum=y[0]; maximum=y[0];
    if (x>1)
        {
        for (int i=1; i<x; i++)
            {
            if ( y[i]<minimum ) { minimum=y[i]; }
            if ( y[i]>maximum ) { maximum=y[i]; }
            }
        }

    yext = (abs(maximum-minimum))/4.0;
    if (yext<10.0) { yext=10.0; }
        
    if ( (minY>minimum) | (maxY<maximum) ) 
        {
        minY = Math.round( minimum - yext );
        maxY = Math.round( maximum + yext );
        }
    
    //--- end of changes, next long inline definition added, v0.52
    
    long tmin = (long)minY;
    long tmax = (long)maxY;
    for (int i=0; i<H_GRID; i++)
        {
        if (((tmax-tmin)%H_GRID)!=0) { tmax++; }
        }
    minY = tmin;
    maxY = tmax;
    
    }

//--- End of method: setDrawData1 ---

public void setDrawData2(int x, double[] y)
    {
    //--- Set array(Y) and array size(X) ---
    drawXsecond=x;                                    // set X-size of used part 
    for (int i=0; i<x; i++) { drawYsecond[i]=y[i]; }  // set array of Y-values
    
    //--- Detect current/minimum/maximum values ---
    valueSecond = y[x-1];
    double localMinimum=y[0], localMaximum=y[0];
    if (x>1)
        {
        for (int i=1; i<x; i++)
            {
            if ( y[i]<localMinimum ) { localMinimum=y[i]; }
            if ( y[i]>localMaximum ) { localMaximum=y[i]; }
            }
        }

        if ( ( localMinimum < minimum ) | ( Double.isNaN(minimum) ) )
            { minimum = localMinimum; }
        if ( ( localMaximum > maximum ) | ( Double.isNaN(maximum) ) ) 
            { maximum = localMaximum; } 
    
    yext = (abs(maximum-minimum))/4.0;
    if (yext<10.0) { yext=10.0; }
        
    if ( (minY>minimum) | (maxY<maximum) ) 
        {
        minY = Math.round( minimum - yext );
        maxY = Math.round( maximum + yext );
        }
    
    //--- end of changes, next long inline definition added, v0.52
    
    long tmin = (long)minY;
    long tmax = (long)maxY;
    for (int i=0; i<H_GRID; i++)
        {
        if (((tmax-tmin)%H_GRID)!=0) { tmax++; }
        }
    minY = tmin;
    maxY = tmax;

    }

//--- End of method: setDrawData2 ---

}


