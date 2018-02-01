//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Modal windows "Report this", "Report all" include dialogues =======

package powerinfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.io.*;

public class ActionReport {
private static JFileChooser fc = new JFileChooser();
private static final String FILE_NAME = "report.txt";
private static FileNameExtensionFilter filter;
private static final int MAXCOL_DEFAULT = 13;
private static final int MAXCOL_LIMIT = 60;
    
//---------- Entry point for RT = "Report this" dialogue method, setup GUI -----
public void createDialogRT
      ( JFrame parentWin ,
        AbstractTableModel atm1 , AbstractTableModel atm2 ,
        String longName , String vendorVersion ) 
    {
    fc.setDialogTitle("Report this - select directory");
    filter = new FileNameExtensionFilter ( "Text files" , "txt" );
    fc.setFileFilter(filter);
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fc.setSelectedFile(new File(FILE_NAME));
    
    //--- (re)start dialogue ---
    boolean inDialogue = true;
    restartDialogue:
    while(inDialogue)
        {
        int select = fc.showSaveDialog(parentWin);
        //--- save file ---
        if(select==JFileChooser.APPROVE_OPTION)
            {
                
            String s1 = fc.getSelectedFile().getPath();
            int x0 = JOptionPane.YES_OPTION;
            //--- check file exist and warning message ---
            File file = new File(s1);
            if( file.exists() == true )
                {
                x0 = JOptionPane.showConfirmDialog
                    ( null, 
                    "File exist: " + s1 + "\noverwrite?" , "REPORT" ,
                    JOptionPane.YES_NO_CANCEL_OPTION ,
                    JOptionPane.WARNING_MESSAGE );  // or QUESTION_MESSAGE
                }
            //--- Select operation by user selection ---
            // if ( x0 == JOptionPane.YES_OPTION )     // reserved, no actions 
            if ( ( x0 == JOptionPane.NO_OPTION  ) |
                 ( x0 == JOptionPane.CLOSED_OPTION ) )
                { continue restartDialogue; }
            if ( x0 == JOptionPane.CANCEL_OPTION ) 
                { inDialogue = false; continue restartDialogue; }
            //--- continue prepare for save file ---
            String s2 = "Report file.\r\n" + 
                        longName + "\r\n" +
                        vendorVersion + "\r\n\r\n";
            String s3 = "" , s4 = "";
            //-- make and save report ---
            if ( atm1 != null ) { s3 = tableReport(atm1); }
            if ( atm2 != null ) { s4 = tableReport(atm2); }
            saveReport( parentWin, s1, s2 + s3 + "\r\n" + s4 );
            //---
            inDialogue = false;
            }  else { inDialogue = false; }
        }    //--- End of save dialogue cycle ---
    }        //--- End of method ---

//---------- Entry point for RF = "Report full" dialogue method, setup GUI -----
public void createDialogRF
      ( JFrame parentWin ,
        AbstractTableModel[] atma1 , AbstractTableModel[] atma2 ,
        String longName , String vendorVersion ) 
    {
    fc.setDialogTitle("Report full - select directory");
    filter = new FileNameExtensionFilter ( "Text files" , "txt" );
    fc.setFileFilter(filter);
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fc.setSelectedFile(new File(FILE_NAME));
    
    //--- (re)start dialogue ---
    boolean inDialogue = true;
    restartDialogue:
    while(inDialogue)
        {
        int select = fc.showSaveDialog(parentWin);
        //--- save file ---
        if(select==JFileChooser.APPROVE_OPTION)
            {
                
            String s1 = fc.getSelectedFile().getPath();
            int x0 = JOptionPane.YES_OPTION;
            //--- check file exist and warning message ---
            File file = new File(s1);
            if( file.exists() == true )
                {
                x0 = JOptionPane.showConfirmDialog
                    ( null, 
                    "File exist: " + s1 + "\noverwrite?" , "REPORT" ,
                    JOptionPane.YES_NO_CANCEL_OPTION ,
                    JOptionPane.WARNING_MESSAGE );  // or QUESTION_MESSAGE
                }
            //--- Select operation by user selection ---
            // if ( x0 == JOptionPane.YES_OPTION )     // reserved, no actions 
            if ( ( x0 == JOptionPane.NO_OPTION  ) |
                 ( x0 == JOptionPane.CLOSED_OPTION ) )
                { continue restartDialogue; }
            if ( x0 == JOptionPane.CANCEL_OPTION ) 
                { inDialogue = false; continue restartDialogue; }
            //--- continue prepare for save file ---
            String s2 = "Report file.\r\n" + 
                        longName + "\r\n" +
                        vendorVersion + "\r\n\r\n";
            String s3 = "";
            //-- make and save report ---
            int n = atma1.length;
            for (int i=0; i<n; i++)
                {
                if ( atma1[i] != null ) 
                    { s3 = s3 + tableReport( atma1[i]) + "\r\n\r\n"; }
                if ( atma2[i] != null ) 
                    { s3 = s3 + tableReport( atma2[i]) + "\r\n"; }
                }
            saveReport( parentWin, s1, s2 + s3 );
            //---
            inDialogue = false;
            }  else { inDialogue = false; }
        }    //--- End of save dialogue cycle ---
    }        //--- End of method ---

//---------- Helper method for convert table model to string -------------------
private static String tableReport (AbstractTableModel atm) {
    String report="";
    if (atm==null) { return report; }
//--- Continue if table exist,get geometry ---    
    int m = atm.getColumnCount();
    int n = atm.getRowCount();
    String s;
    int a;
    int[] maxcols = new int[m];
    // int maxcol=13;
    int maxcol = MAXCOL_DEFAULT;
//--- Get column names lengths ---    
    for (int i=0; i<m; i++)
        { maxcols[i] = atm.getColumnName(i).length(); }
//--- Get column maximum lengths ---    
    for (int j=0; j<n; j++)
        {
        for (int i=0; i<m; i++)
            {
            // s = (String)atm.getValueAt(j,i);
            s = getShortString( atm, j, i );
            //
            a = s.length();
            if (a>maxcols[i]) { maxcols[i]=a; }
            }
        }
    for ( int i=0; i<maxcols.length; i++ ) { maxcol += maxcols[i]; }
//--- Write table up ---    
    for (int i=0; i<m; i++)
        {
        s = atm.getColumnName(i);
        report = report + " " + s;
        a = maxcols[i] - s.length() + 1;
        for (int k=0; k<a; k++) { report = report + " "; }
        }
//--- Write horizontal line ---        
    report = report + "\r\n";
    for (int i=0; i<maxcol; i++) { report = report + "-"; }
    report = report + "\r\n";
//--- Write table content ---    
    for (int j=0; j<n; j++)       // this cycle for rows , n = rows count
        {
        for (int i=0; i<m; i++)   // this cycle for columns , m = columns count
            {
            // s = " " + (String)atm.getValueAt(j,i);
            s = getShortString( atm, j, i );
            //
            report = report + s;
            a = maxcols[i] - s.length() + 2;
            for (int k=0; k<a; k++) { report = report + " "; }
            }
            report = report + "\r\n";    
        }
//---Write horizontal line ---    
    for (int i=0; i<maxcol; i++) { report = report + "-"; }
    report = report + "\r\n";
//--- Return ---    
    return report; }

//----------Helper method for get short version of strings for report ----------
private static String getShortString ( AbstractTableModel atm, int j, int i )
    {
    String s1 = " " + (String)atm.getValueAt(j,i);
    String s2 = s1;
    int n = MAXCOL_LIMIT;
    int m = n-3;
    if ( s1.length() > n ) 
        {
        s2 = "";
        for ( int k=0; k<m; k++ ) { s2 = s2 + s1.charAt(k); }
        s2 = s2 + "...";
        }
    return s2;
    }

//---------- Helper method for save string to file and visual status -----------
private static void saveReport
                   ( JFrame parentWin, String filePath, String fileData ) {
    int status=0;
    try ( FileWriter writer = new FileWriter(filePath, false) )
        { writer.write(fileData); writer.flush(); }
    catch(Exception e) { status=1; }
            
    if (status==0)  {
                    JOptionPane.showMessageDialog
                    (parentWin, "Report saved: " + filePath, "REPORT",
                    JOptionPane.WARNING_MESSAGE); }
            else    {
                    JOptionPane.showMessageDialog
                    (parentWin, "Write report failed", "ERROR",
                    JOptionPane.ERROR_MESSAGE); }
    }

}

