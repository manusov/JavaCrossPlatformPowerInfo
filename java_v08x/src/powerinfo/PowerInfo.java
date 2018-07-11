/*

Java Power Info utility, (C)2018 IC Book Labs
Main class of the project
USE JDK7 MODE.
JDK8 from v0.81.

---------- How design Linux version, variant 1 ---------------------------------

--- Step 1, learn Linux power management ---
User interface design:
  Screen 1 = Power status brief info, get text data,
  Screen 2 = Battery details full info, get text data,
  Screen 3 = Charging monitor, get numeric data,
  Screen 4 = OS information, get text data,
  Screen 5 = Java cross-platform information, no changes, 
  Plus ennumeration path get array of text strings.

--- Step 2, formalization with existed API architecture ---
Build classes:
  TableModelBatteryDetailsLinux = override TableModelBatteryDetails, 
  TableModelPowerStatusLinux = override TableModelPowerStatus,
  TableModelLinux = override TableModelWindows.
Native methods not required, use "/proc" filesystem under Linux.
tmbd = f(OS type)
tmps = f(OS type)
tmos = f(OS type)

--- Step 3, optimization ---
Remove Windows-centric assymetry, make abstract class or interface for
this classes, instantiation or implementation = f(OS type),
replace model:
  Primary=Windows , Override=Linux
to model: 
  Primary=Abstract , Override = Windows or Linux, f(OS type).

---------- How design Linux version, variant 2 ---------------------------------
Linux features effect maximized

Screen 1 = Power Status, tree and table based on "/sys" filesystem subtree.
Screen 2 = Battery details, tree and table based on "/sys" filesystem subtree.
Screen 3 = Charging monitor, fast get one parameter by "/sys/ ... fixed ...". 
Screen 4 = Linux (instead Windows), full tree of "/sys" filesystem.
Screen 5 = Java, not changed because cross-platform.

*/

package powerinfo;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import powerinfo.supportwindows.*;
import powerinfo.supportlinux.*;
import powerinfo.supportcpu.*;

// Main class declaration start
public class PowerInfo
{
// Main class fields

private static final int X_SIZE = 580, Y_SIZE = 488+40;    // window size
    
public static PAL pal;              // platform abstraction layer, native calls

private static int jniStatus;       // status of java native interface call
private static Timer timer1;        // timer for revisualization
private static boolean ctoggle;     // draw mode active flag 
private static int nativeID;        // 0=Win32, 1=Win64, 2=Linux32, 3=Linux64

private static DataModelChargingMonitor bcm1, bcm2;   // drawings mod.
private static GraphIcon gic1, gic2;                  // drawings ics.
private static TableModelPowerStatus atmAC;        // AC power table model
private static TableModelBatteryDetails atmBAT;    // Battery power table model

private static GUIbox guiBox;                      // window class
private static JButton b1, b2, b3, b4;             // down buttons
private static JTabbedPane rootMenu;               // root menu
private static JPanel downButtons;                 // buttons panel
private static JPanel[] rootPanels;                // panels for tabs
private static JTable rvt1, rvt2;                  // timer revisualized table
private static JPanel rvp1;                        // timer revisualized panel
private static JCheckBox[] drawChecks;             // enable drawings for bats
private static boolean[] drawFlags;                // flags for checkboxes

private static final String[] NAMES_WINDOWS =      // tabs names for Windows
    { "Power status"     , 
      "Battery details"  ,
      "Charging monitor" ,
      "Windows"          ,
      "Java"             };
private static final String[] NAMES_LINUX =         // tabs names for Linux
    { "AC power"         , 
      "Battery"          ,
      "Charging monitor" ,
      "/sys"             ,
      "/proc"            ,
      "Java"             };
private static final String[] NAMES_COMMON =        // tabs names for both w/l
    { "CPU"              };
private static String[] panelsNames;                // result names array=f(OS)

private static final String AC_ENUM  = "AC source path";  // jcombo names
private static final String BAT_ENUM = "Battery path";

private static AbstractTableModel[] reportTables1, reportTables2;  // reports

// Application entry point
public static void main(String[] args) 
    {
    // Setup variables and GUI options
    ctoggle = false;    // charging monitor drawings enable
    nativeID = -1;      // 0=Win32, 1=Win64, 2=Linux32, 3=Linux64, -1=Unknown
    JFrame.setDefaultLookAndFeelDecorated(true);   // Decorate main window
    JDialog.setDefaultLookAndFeelDecorated(true);  // Decorate dialog windows
    // Load native library
    pal = new PAL();               // Initializing Platform Abstraction Layer
    jniStatus = pal.loadUserModeLibrary();      // Load OS-specific library
    nativeID = pal.getNativeType();             // This actual after load try
    if ( ! ( (nativeID==2) | (nativeID==3) ) )  // Linux mode w/o native lib
        {
        if ( ( jniStatus < 0 ) | ( pal.getNativeValid() == false ) )
        JOptionPane.showMessageDialog
            ( null, "Native library load failed" , About.getShortName() ,
            JOptionPane.ERROR_MESSAGE ); 
        }
 
// --- Start common branch (WINDOWS, LINUX) ---

    // Initializing Processor support
    JPanel rp10 = new JPanel();
    ViewerTree vp10 = new ViewerTree( X_SIZE, Y_SIZE , null , null , null );
    if ( ( nativeID >= 0 ) & ( pal.getNativeValid() ) )
        {
        TreeModelCPU trmc = new TreeModelCPU();        // includes TSC measure
        TreeMonitorCPU tmcp = new TreeMonitorCPU();    // monitor node expand
        ArrayList<DefaultMutableTreeNode> list10 = trmc.getCpuList();
        DefaultTreeModel dtm10 = new DefaultTreeModel( list10.get(0) , true );
        AbstractTableModel atm10 = new TableModelCPU();
        TreeNodeExpander cputnx1 = new CPUTNX1( dtm10 , list10 );
        vp10 = new ViewerTree( X_SIZE, Y_SIZE , dtm10 , atm10 , cputnx1 );
        cputnx1.setViewer(vp10);   // expander must have access to table
        cputnx1.setMonitor(tmcp);  // expander must call tree monitor
        
        double clk;
        clk = trmc.getTscClkHz();                     // measure and return clk
        TableModelClk tclk = new TableModelClk(clk);  // create model replacer
        tmcp.setModel(tclk);             // assign table model for tree monitor
        
        rp10 = vp10.getP();
        }
    
// --- WINDOWS BRANCH ---

    if ( (nativeID==0) | (nativeID==1) )
    {
    int n1 = NAMES_WINDOWS.length;   // n1 = Number of Windows-specific panels
    int n2 = NAMES_COMMON.length;    // n2 = Number of Common panels
    int n = n1 + n2;                 // n = Total number of panels
    int j = 0;                       // j = Total counter
    panelsNames = new String[n];
    for (int i=0; i<n1; i++ ) { panelsNames[j] = NAMES_WINDOWS[i]; j++; }
    for (int i=0; i<n2; i++ ) { panelsNames[j] = NAMES_COMMON[i];  j++; }
    
    // AC power window
    
    String acPath  = "AC power supply";          // Value of combo box
    atmAC = new TableModelPowerStatusWindows();  // Table model
    ACselect cch1 = new ACselect();              // Listener for combo box
    final ViewerTableCombo vp1 =                 // Panel = Combo + Table
        new ViewerTableCombo( X_SIZE, Y_SIZE, AC_ENUM, cch1, atmAC );
    vp1.getCombo().addItem(acPath);              // Set value for combo
    JPanel rp1 = vp1.getP();                     // GUI panel, one of tabs
    
    // Battery power window
    
    atmBAT = new TableModelBatteryDetailsWindows();  // Table model
    String[] batPaths = atmBAT.getEnumerationPath(); // Get paths set
    int np = 0;
    if (batPaths!=null)
        {
        np = batPaths.length;
        }
    atmBAT.setSelected(0);                           // Make selection for table
    BATselect cch2 = new BATselect();                // Listener for combo box
    final ViewerTableCombo vp2 =                     // Panel = Combo + Table
        new ViewerTableCombo( X_SIZE, Y_SIZE, BAT_ENUM , cch2, atmBAT );
    for( int i=0; i<np;  i++ )                       // Set value for combo
        {
        vp2.getCombo().addItem( batPaths[i] );
        }
    JPanel rp2 = vp2.getP();                         // GUI panel, one of tabs
    // Add listener for combo box: battery enumeration path
    vp2.getCombo().addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e)
        { 
        JComboBox box = (JComboBox)e.getSource();
        ComboBoxModel cbm = box.getModel();
        Object ob = cbm.getSelectedItem();
        int sel=0;
        int n = cbm.getSize();
        for ( sel=0; sel<n; sel++ )
            {
            if ( ob.equals(cbm.getElementAt(sel)) ) break;
            }
        atmBAT.setSelected(sel); 
        rvt2.revalidate();
        rvt2.repaint(); 
        rvp1.revalidate();
        rvp1.repaint();
        } } );                // Set to table model
    
// Charging monitor window

    // Graph1: current level, mWh = F (Time, seconds)
    gic1 = new GraphIcon(560,161);                    // Up icon, draw y=f(x)
    // Graph2: current rate, mW = F (Time, seconds)
    gic2 = new GraphIcon(560,161);                    // Down icon, draw y=f(x)
    // Data model
    int a = gic1.getUsedWidth();
    int b = gic2.getUsedWidth();
    if ( a<b ) { a=b; }
    bcm1 = new DataModelChargingMonitor(a);           // Data model = f(Xsize)
    bcm2 = new DataModelChargingMonitor(a);
    // Setup power units: mWh/mW or Relative ratio units
    boolean c;
    c = atmBAT.getPowerUnits();
    bcm1.setPowerUnits(c);
    bcm2.setPowerUnits(c);
    // Text strings, get from model, set to icons
    String s;
    s = bcm1.getNameX1();  gic1.setNameX(s);         // Text strings, up icon
    s = bcm1.getNameY1();  gic1.setNameY(s);
    s = bcm1.getNameX2();  gic2.setNameX(s);         // Text strings, down icon
    s = bcm1.getNameY2();  gic2.setNameY(s);
    // Select units
    double miny1, maxy1, miny2, maxy2;
    if (c)
        {                       // this values for Relative ratio units
        miny1 = 0.0;
        maxy1 = 120.0;
        miny2 = -100.0;
        maxy2 = 100.0;
        }
    else
        {                       // this values for mWh/mW
        miny1 = 45000.0;
        maxy1 = 60000.0;
        miny2 = -3000.0;
        maxy2 = 3000.0;
        }
    gic1.setMinX(0.0);   gic1.setMaxX(a/2);       // Dimensions, up icon 
    gic1.setMinY(miny1); gic1.setMaxY(maxy1);
    gic2.setMinX(0.0);   gic2.setMaxX(a/2);       // Dimensions, down icon
    gic2.setMinY(miny2); gic2.setMaxY(maxy2);
    // Build viewer
    int nb = 0;
    Vector drawVector = new Vector();
    if (batPaths!=null)
        {
        nb = batPaths.length;
        drawVector = new Vector();
        drawChecks = new JCheckBox[nb];
        for( int i=0; i<nb; i++ )
            {
            drawChecks[i] = new JCheckBox( batPaths[i], false );
            drawVector.add( drawChecks[i] );
            }
        drawChecks[0].setSelected(true);
        }
    else
        {
        drawVector.add("N/A");
        }
    final ViewerPowerDraws vp3 =             // Panel = 2 draws + 2 buttons 
            new ViewerPowerDraws( X_SIZE, Y_SIZE, bcm1, gic1, gic2, 
                                  BAT_ENUM, drawVector );          
    JPanel rp3 = vp3.getP();                  // GUI panel, one of tabs
    // Add listeners for checkboxes, with build array
    drawFlags = new boolean[10];
    for( int i=0; i<10; i++ ) { drawFlags[i] = false; }
    drawFlags[0] = true;
    for( int i=0; i<nb; i++ )
        {
        drawChecks[i].addChangeListener(new ChangeListener() {
        @Override public void stateChanged(ChangeEvent e)
            {
            for(int i=0; i<drawChecks.length; i++ )
                {
                drawFlags[i] = drawChecks[i].getModel().isSelected();
                }
            gic1.clearMeasure();
            gic2.clearMeasure();
            bcm1.clearArrayY1();
            bcm1.clearArrayY2();
            bcm2.clearArrayY1();
            bcm2.clearArrayY2();
            } });
        }
    // Add listener for Graph CLEAR button
    vp3.getClearButton().addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e)
        { gic1.clearMeasure();      // This executed when press CLEAR button
          gic2.clearMeasure();      // clear drawings icon
          bcm1.clearArrayY1();
          bcm1.clearArrayY2();      // clear model
          bcm2.clearArrayY1();
          bcm2.clearArrayY2();      // clear model
          ctoggle=false;            // disable drawings
          vp3.getRunButton().setText("Run"); } } );  // "Stop" replaced to "Run"
    // Add listener for Graph RUN button
    vp3.getRunButton().addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e)
        { if (ctoggle==true)   // Check current state: STOP or RUN 
               {               // If current state is STOP, set RUN
               vp3.getRunButton().setText("Run"); 
               }
          else {               // If current state is RUN, set STOP
               vp3.getRunButton().setText("Stop");
               }
          ctoggle=!ctoggle;     // Invert status "Run" / "Stop"
        } } );

    // Windows OS info window

    TableModelOs atm4 = new TableModelWindows();    // Windows info table model 
    ViewerTable vp4 = new ViewerTable( X_SIZE, Y_SIZE, atm4 );  // Panel = Table
    JPanel rp4 = vp4.getP();                        // GUI panel, one of tabs

    // Java info window
    
    TableModelJava atm5 = new TableModelJava();     // Java info table model
    ViewerTable vp5 = new ViewerTable( X_SIZE, Y_SIZE, atm5 );  // Panel = Table
    JPanel rp5 = vp5.getP();                        // GUI panel, one of tabs
    
    // Build result of windows-specific branch
    // Create array of panels for visual and array of table models for report,
    // plus special pool for dynamical revisual

    rootPanels = new JPanel[] { rp1, rp2, rp3, rp4, rp5, rp10 };
    reportTables1 = new AbstractTableModel[]
        {                                                // BUG, NEED GET LATER
        (AbstractTableModel) vp1.getTable().getModel() ,
        (AbstractTableModel) vp2.getTable().getModel() ,
        null ,
        (AbstractTableModel) vp4.getTable().getModel() ,
        (AbstractTableModel) vp5.getTable().getModel() ,
        (AbstractTableModel) vp10.getTable().getModel() ,
        };
    reportTables2 = new AbstractTableModel[]   // Second array required, because
        { null, null, null, null, null, null }; // 2 tables supported at CPUID
    rvp1 = rp3;                // drawings panel, supported dynamical revisual
    rvt1 = vp1.getTable();     // AC panel, supported dynamical revisual 
    rvt2 = vp2.getTable();     // BAT panel, supported dynamical revisual 
    }

// --- LINUX BRANCH ---

    else if ( (nativeID==2) | (nativeID==3) )  // 2=Linux32, 3=Linux64
    {
    
    int n1 = NAMES_LINUX.length;     // n1 = Number of Linux-specific tabs
    int n2 = NAMES_COMMON.length;    // n2 = Number of Common tabs
    int n = n1 + n2;                 // n = Total number of panels (tabs)
    int j = 0;                       // j = Total counter 
    panelsNames = new String[n];
    for (int i=0; i<n1; i++ ) { panelsNames[j] = NAMES_LINUX[i]; j++; }
    for (int i=0; i<n2; i++ ) { panelsNames[j] = NAMES_COMMON[i];  j++; }
    
    String acPath  = "/sys/class/power_supply/AC";     // Linux Path for AC
    String batPath;                                    // Linux Path for BAT
    TreeModelLinux trml = new TreeModelLinux();        // Models "/sys", "/proc"
    TreeMonitorLinux tmlx = new TreeMonitorLinux();    // Node open monitor

    // AC power window

    atmAC = new TableModelPowerStatusLinux();   // Table model for AC
    ACselect cch1 = new ACselect();             // Listener for combo box
    final ViewerTableCombo vp1 =                // Panel = Combo + Table 
        new ViewerTableCombo( X_SIZE, Y_SIZE, AC_ENUM, cch1, atmAC );
    vp1.getCombo().addItem(acPath);             // Set one value for combo
    JPanel rp1 = vp1.getP();                    // GUI panel, one of tabs

    // Battery power window

    atmBAT = new TableModelBatteryDetailsLinux();     // Table model for BAT
    String[] batPaths = atmBAT.getEnumerationPath();  // Get paths set
    batPath = "";
    if (batPaths!=null)
        {
        batPath = batPaths[0];                // First path from set
        }
    BATselect cch2 = new BATselect();            // Listener for combo box
    final ViewerTableCombo vp2 =                 // Panel = Combo + Table
        new ViewerTableCombo( X_SIZE, Y_SIZE, BAT_ENUM , cch2, atmBAT );
    vp2.getCombo().addItem(batPath);            // Set one value for combo
    JPanel rp2 = vp2.getP();                    // GUI panel, one of tabs
    // Add listener for combo box: enumeration path
    vp2.getCombo().addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e)
        { 
        int sel = vp2.getCombo().getSelectedIndex();  // Get from combo
        atmBAT.setSelected(sel); } } );               // Set to table model

    // Charging monitor window

    // Graph1: current level, mWh = F (Time, seconds)
    gic1 = new GraphIcon(560,161);                       // Up icon, y=f(x)
    // Graph2: current rate, mW = F (Time, seconds)
    gic2 = new GraphIcon(560,161);                       // Down icon, y=f(x)
    // Data model
    int a = gic1.getUsedWidth();
    int b = gic2.getUsedWidth();
    if ( a<b ) { a=b; }
    bcm1 = new DataModelChargingMonitor(a);      // Setup data model = f(Xsize)
    bcm2 = new DataModelChargingMonitor(a);      // Setup data model = f(Xsize)
    // Text strings, get from model, set to icons
    String s;
    s = bcm1.getNameX1();  gic1.setNameX(s);         // Text strings, up icon
    s = bcm1.getNameY1();  gic1.setNameY(s);
    s = bcm1.getNameX2();  gic2.setNameX(s);         // Text strings, down icon
    s = bcm1.getNameY2();  gic2.setNameY(s);
    // Dimensions, set constants to icons
    gic1.setMinX(0.0);     gic1.setMaxX(a/2);       // Dimensions, up icon
    gic1.setMinY(45000.0); gic1.setMaxY(60000.0);
    gic2.setMinX(0.0);     gic2.setMaxX(a/2);       // Dimensions, down icon
    gic2.setMinY(-3000.0); gic2.setMaxY(3000.0);
    // Build viewer
    int nb = 0;
    Vector drawVector = new Vector();
    if (batPaths!=null)
        {
        nb = batPaths.length;
        drawVector = new Vector();
        drawChecks = new JCheckBox[nb];
        for( int i=0; i<nb; i++ )
            {
            drawChecks[i] = new JCheckBox( batPaths[i], false );
            drawVector.add( drawChecks[i] );
            }
        drawChecks[0].setSelected(true);
        }
    else
        {
        drawVector.add("N/A");
        }
    final ViewerPowerDraws vp3 =          // Panel = 2 drawings + 2 buttons 
            new ViewerPowerDraws( X_SIZE, Y_SIZE, bcm1, gic1, gic2, 
                                  BAT_ENUM, drawVector );          
    JPanel rp3 = vp3.getP();              // GUI panel, one of tabs
    // Add listeners for checkboxes, with build array
    drawFlags = new boolean[10];
    for( int i=0; i<10; i++ ) { drawFlags[i] = false; }
    drawFlags[0] = true;
    for( int i=0; i<nb; i++ )
        {
        drawChecks[i].addChangeListener(new ChangeListener() {
        @Override public void stateChanged(ChangeEvent e)
            {
            for(int i=0; i<drawChecks.length; i++ )
                {
                drawFlags[i] = drawChecks[i].getModel().isSelected();
                }
            gic1.clearMeasure();
            gic2.clearMeasure();
            bcm1.clearArrayY1();
            bcm1.clearArrayY2();
            bcm2.clearArrayY1();
            bcm2.clearArrayY2();
            } });
        }
    // Add listener for Graph Clear button
    vp3.getClearButton().addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e)
        { gic1.clearMeasure();         // This executed when press CLEAR button
          gic2.clearMeasure();
          bcm1.clearArrayY1();
          bcm1.clearArrayY2();
          bcm2.clearArrayY1();
          bcm2.clearArrayY2();
          ctoggle=false;
          vp3.getRunButton().setText("Run"); } } );
    // Add listener for Graph Run button
    vp3.getRunButton().addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e)
        { if (ctoggle==true)          // This executed when press RUN button
               {
               vp3.getRunButton().setText("Run");   // Set "RUN" if now "STOP"
               }
          else {
               vp3.getRunButton().setText("Stop");  // Set "STOP" if now "RUN"
               }
          ctoggle=!ctoggle;   // Invert state "RUN" / "STOP"
        } } );
    
    // Linux /sys window

    ArrayList<DefaultMutableTreeNode> list1 = trml.getSysList();
    DefaultTreeModel dtm1 = new DefaultTreeModel( list1.get(0) ,true );
    AbstractTableModel atm1 = new STM1();
    TreeNodeExpander tnx1 = new TNX1( dtm1, list1 );
    final ViewerTree vp4 = new ViewerTree( X_SIZE, Y_SIZE, dtm1, atm1, tnx1 );
    tnx1.setViewer(vp4);       // Opener must read table from viewer
    tnx1.setMonitor(tmlx);     // Opener must use node-specific open methods
    JPanel rp4 = vp4.getP();   // GUI Panel = Tree + Table

    // Linux /proc window

    ArrayList<DefaultMutableTreeNode> list2 = trml.getProcList();
    DefaultTreeModel dtm2 = new DefaultTreeModel( list2.get(0) ,true );
    AbstractTableModel atm2 = new PTM1();
    TreeNodeExpander tnx2 = new TNX1( dtm2, list2 );
    final ViewerTree vp5 = new ViewerTree( X_SIZE, Y_SIZE, dtm2, atm2, tnx2 );
    tnx2.setViewer(vp5);       // Opener must read table from viewer
    tnx2.setMonitor(null);     // Opener must use node-specific open methods
    JPanel rp5 = vp5.getP();   // GUI Panel = Tree + Table

    // Java window
    
    TableModelJava atm6 = new TableModelJava();    // Table model for Java
    ViewerTable vp6 = new ViewerTable( X_SIZE, Y_SIZE, atm6 ); // Panel = Table
    JPanel rp6 = vp6.getP();                       // GUI panel, one of tabs

    // Build result of linux-specific branch
    // Create array of panels for visual and array of table models for report,
    // plus special pool for dynamical revisual

    rootPanels = new JPanel[] { rp1, rp2, rp3, rp4, rp5, rp6, rp10 };
    reportTables1 = new AbstractTableModel[]
        {                                                // BUG, NEED GET LATER
        (AbstractTableModel) vp1.getTable().getModel() ,
        (AbstractTableModel) vp2.getTable().getModel() ,
        null ,
        (AbstractTableModel) vp4.getTable().getModel() ,
        (AbstractTableModel) vp5.getTable().getModel() ,
        (AbstractTableModel) vp6.getTable().getModel() ,
        (AbstractTableModel) vp10.getTable().getModel() 
        };
    reportTables2 = new AbstractTableModel[]          // Second array required
        { null, null, null, null, null, null, null }; // because CPUID, 2 tables
    rvp1 = rp3;                // drawings panel, supported dynamical revisual
    rvt1 = vp1.getTable();     // AC panel, supported dynamical revisual 
    rvt2 = vp2.getTable();     // BAT panel, supported dynamical revisual        
    }

// --- BRANCHES CONVERGENTION, RETURN IF UNRECOGNIZED OS ---

    else { System.exit(0); }  // Exit if native OS not detected
    
    // Create dynamical revisualization timer
    // For Windows required native DLL, for Linux support by Java code
    if ( (pal.getNativeValid()==true) | (nativeID==2) | (nativeID==3) )
    {
    timer1 = new Timer();                      // Create timer
    timer1.schedule( new TimerTask() {         // Create task for timer
    @Override public void run() {              // Entry point to timer handler
        try { EventQueue.invokeLater( new Runnable()
                { @Override synchronized public void run()
                    {
                    //--- Drawings if enabled ---
                    if (ctoggle==true)
                    { 
                    double[][] allStamps = atmBAT.readAllStamps();
                    if ( allStamps==null )
                    {
                    double stamp1 = atmBAT.getStamp1(); // Get from table models
                    double stamp2 = atmBAT.getStamp2();
                    if ( ( stamp1 != Double.NaN ) && ( stamp2 != Double.NaN ) )
                        {
                        bcm1.pushDataY1( stamp1 );       // Set to data models
                        bcm1.pushDataY2( stamp2 );
                        gic1.setDrawData1
                            ( bcm1.getSizeX1(), bcm1.getArrayY1() );
                        gic2.setDrawData1
                            ( bcm1.getSizeX2(), bcm1.getArrayY2() );
                        }
                    }
                    else
                    {
                        double stamp1;
                        double stamp2;
                        if ( drawFlags[0]==true )
                        {
                        stamp1 = allStamps[0][0];
                        stamp2 = allStamps[0][1];
                        if (( stamp1 != Double.NaN )&&( stamp2 != Double.NaN ))
                            {
                            bcm1.pushDataY1( stamp1 );    // Set to data models
                            bcm1.pushDataY2( stamp2 );
                            gic1.setDrawData1
                                ( bcm1.getSizeX1(), bcm1.getArrayY1() );
                            gic2.setDrawData1
                                ( bcm1.getSizeX2(), bcm1.getArrayY2() );
                            }
                        }
 
                        if ( drawFlags[1]==true )
                        {
                        stamp1 = allStamps[1][0];
                        stamp2 = allStamps[1][1];
                        if (( stamp1 != Double.NaN )&&( stamp2 != Double.NaN ))
                            {
                            bcm2.pushDataY1( stamp1 );    // Set to data models
                            bcm2.pushDataY2( stamp2 );
                            gic1.setDrawData2
                                ( bcm2.getSizeX1(), bcm2.getArrayY1() );
                            gic2.setDrawData2
                                ( bcm2.getSizeX2(), bcm2.getArrayY2() );
                            }
                        }
                    }
                    
                    }
                    // Dynamical revisual support
                    if ( atmAC.DynamicalPowerStatus() )
                       { rvt1.revalidate(); rvt1.repaint(); }
                         rvp1.revalidate(); rvp1.repaint();
                    if ( atmBAT.DynamicalBatteryDetails() )
                       { rvt2.revalidate(); rvt2.repaint(); }
                         rvp1.revalidate(); rvp1.repaint();
                    }
                } );
            } catch(Exception e1) { e1.printStackTrace(); }
        } }, 0, 500 );  // Initial delay=0, period=500 ms
    }
    
    // Buttons
    b1 = new JButton("About");
    b2 = new JButton("Report this");
    b3 = new JButton("Report all");
    b4 = new JButton("Cancel");
    downButtons = new JPanel();
    BoxLayout boxx = new BoxLayout(downButtons, BoxLayout.X_AXIS);
    downButtons.setLayout(boxx);
    downButtons.add(Box.createHorizontalGlue());    // Left interval
    downButtons.add(b1);
    downButtons.add(Box.createHorizontalStrut(2));  // Interval between buttons
    downButtons.add(b2);
    downButtons.add(Box.createHorizontalStrut(2));
    downButtons.add(b3);
    downButtons.add(Box.createHorizontalStrut(2));
    downButtons.add(b4);
    downButtons.add(Box.createHorizontalGlue());    // Right interval

    // Add listener for button #1: "About"
    b1.addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e) {
    ActionAbout about = new ActionAbout();
    final JDialog dialog = about.createDialog
        ( guiBox , About.getShortName() , About.getVendorName() );
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true); }});

    // Add listener for button #2: "Report this" (RT)
    b2.addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e) {
    int i=rootMenu.getSelectedIndex();
    ActionReport aReport = new ActionReport();
    aReport.createDialogRT
        ( guiBox , reportTables1[i] , reportTables2[i] ,   // Table = f(tab) 
          About.getShortName() , About.getVendorName() );  }});

    // Add listener for button #3: "Report all" (RA)
    b3.addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e) {
    ActionReport aReport = new ActionReport();
    aReport.createDialogRF
        ( guiBox , reportTables1 , reportTables2 ,     // Tables for all tabs
          About.getShortName() , About.getVendorName() );  }});

    // Add listener for button #4: "Cancel"
    b4.addActionListener(new ActionListener() {
    @Override public void actionPerformed(ActionEvent e)
        { System.exit(0); } } );
    
    // Build Tabbed Panel
    rootMenu = new JTabbedPane();
    for ( int i=0; i<rootPanels.length; i++ )
        {                            // Add panels with names to JTabbedPane
        rootMenu.add( rootPanels[i] , panelsNames[i] );
        }
 
    // Build GUI
    // SpringLayout putConstraint format (a,b,c,d)
    // a = positioned element size (NORTH, SOUTH, WEST, EAST)
    // b = positioned element name
    // c = distance from anchor element, can be positive or negative
    // d = anchor element
    guiBox = new GUIbox(About.getShortName());
    SpringLayout sl = new SpringLayout();
    Container c = guiBox.getContentPane();
    c.setLayout(sl);
    sl.putConstraint
        (SpringLayout.NORTH, rootMenu, 1, SpringLayout.NORTH, c);
    sl.putConstraint
        (SpringLayout.SOUTH, rootMenu, -45, SpringLayout.SOUTH, c);
    sl.putConstraint
        (SpringLayout.WEST, rootMenu, 0,  SpringLayout.WEST , c);
    sl.putConstraint
        (SpringLayout.EAST, rootMenu, 0,  SpringLayout.EAST , c);
    sl.putConstraint
        (SpringLayout.NORTH, downButtons, 2, SpringLayout.SOUTH, rootMenu);
    sl.putConstraint
        (SpringLayout.SOUTH, downButtons, -1, SpringLayout.SOUTH, c);
    sl.putConstraint
        (SpringLayout.WEST, downButtons, 0, SpringLayout.WEST, c);
    sl.putConstraint
        (SpringLayout.EAST, downButtons, 0, SpringLayout.EAST, c);
    c.add(rootMenu);                                 // add JTabbedPane
    c.add(downButtons);                              // add panel with buttons
    guiBox.setDefaultCloseOperation(EXIT_ON_CLOSE);  // discipline for close
    guiBox.setLocationRelativeTo(null);              // desktop centering option
    guiBox.setSize(X_SIZE, Y_SIZE);                  // set size
    guiBox.setVisible(true);                         // make GUI window visible
    }  // end of main method

}      // end of class
    
