/*
Java Power Info utility, (C)2021 IC Book Labs
Modal windows "Report this", "Report all" include dialogues
*/

package powerinfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.io.*;

public class ActionReport {
private static final JFileChooser FILE_CH = new JFileChooser();
private static final String FILE_NAME = "report.txt";
private static FileNameExtensionFilter filter;
private static final int MAXCOL_DEFAULT = 13;
private static final int MAXCOL_LIMIT = 60;
    
// Entry point for RT = "Report this" dialogue method, setup GUI
public void createDialogRT
      ( JFrame parentWin ,
        AbstractTableModel atm1 , AbstractTableModel atm2 ,
        String longName , String vendorVersion ) 
    {
    FILE_CH.setDialogTitle( "Report this - select directory" );
    filter = new FileNameExtensionFilter ( "Text files" , "txt" );
    FILE_CH.setFileFilter( filter );
    FILE_CH.setFileSelectionMode( JFileChooser.FILES_ONLY );
    FILE_CH.setSelectedFile( new File( FILE_NAME ) );
    // (re)start dialogue
    boolean inDialogue = true;
     while( inDialogue )
        {
        int select = FILE_CH.showSaveDialog( parentWin );
        // save file
        if(select==JFileChooser.APPROVE_OPTION)
            {
            String s1 = FILE_CH.getSelectedFile().getPath();
            StringBuilder sb1 = new StringBuilder( "" );
            sb1.append( "File exist: " );
            sb1.append( s1 );
            sb1.append( "\noverwrite?" );
            int x0 = JOptionPane.YES_OPTION;
            // check file exist and warning message
            File file = new File( s1 );
            if( file.exists() == true )
                {
                x0 = JOptionPane.showConfirmDialog
                    ( null , sb1.toString() , "REPORT" ,
                    JOptionPane.YES_NO_CANCEL_OPTION ,
                    JOptionPane.WARNING_MESSAGE );  // or QUESTION_MESSAGE
                }
            // Select operation by user selection
            // if ( x0 == JOptionPane.YES_OPTION )     // reserved, no actions 
            if ( ( x0 == JOptionPane.NO_OPTION  ) |
                 ( x0 == JOptionPane.CLOSED_OPTION ) )
                { continue; }
            if ( x0 == JOptionPane.CANCEL_OPTION ) 
                { inDialogue = false; continue; }
            // continue prepare for save file
            StringBuilder sb2 = new StringBuilder( "" );
            sb2.append( "Report file.\r\n" );
            sb2.append( longName );
            sb2.append( "\r\n" );
            sb2.append( vendorVersion );
            sb2.append( "\r\n\r\n" );
            String s2 = "" , s3 = "";
            // make and save report
            if ( atm1 != null ) { s2 = tableReport(atm1); }
            if ( atm2 != null ) { s3 = tableReport(atm2); }
            sb2.append( s2 );
            sb2.append( "\r\n" );
            sb2.append( s3 );
            saveReport( parentWin, s1, sb2.toString() );
            // after save report
            inDialogue = false;
            }  else { inDialogue = false; }
        }    // End of save dialogue cycle
    }        // End of method

// Entry point for RF = "Report full" dialogue method, setup GUI
public void createDialogRF
      ( JFrame parentWin ,
        AbstractTableModel[] atma1 , AbstractTableModel[] atma2 ,
        String longName , String vendorVersion ) 
    {
    FILE_CH.setDialogTitle( "Report full - select directory" );
    filter = new FileNameExtensionFilter ( "Text files" , "txt" );
    FILE_CH.setFileFilter( filter );
    FILE_CH.setFileSelectionMode( JFileChooser.FILES_ONLY );
    FILE_CH.setSelectedFile( new File( FILE_NAME ) );
    // (re)start dialogue
    boolean inDialogue = true;
    while( inDialogue )
        {
        int select = FILE_CH.showSaveDialog( parentWin );
        // save file
        if( select == JFileChooser.APPROVE_OPTION )
            {
            String s1 = FILE_CH.getSelectedFile().getPath();
            StringBuilder sb1 = new StringBuilder( "" );
            sb1.append( "File exist: " );
            sb1.append( s1 );
            sb1.append( "\noverwrite?" );
            int x0 = JOptionPane.YES_OPTION;
            // check file exist and warning message
            File file = new File(s1);
            if( file.exists() == true )
                {
                x0 = JOptionPane.showConfirmDialog
                    ( null , sb1.toString() , "REPORT" ,
                    JOptionPane.YES_NO_CANCEL_OPTION ,
                    JOptionPane.WARNING_MESSAGE );  // or QUESTION_MESSAGE
                }
            // Select operation by user selection
            if ( ( x0 == JOptionPane.NO_OPTION  ) |
                 ( x0 == JOptionPane.CLOSED_OPTION ) )
                { continue; }
            if ( x0 == JOptionPane.CANCEL_OPTION ) 
                { inDialogue = false; continue; }
            // continue prepare for save file
            StringBuilder sb2 = new StringBuilder( "" );
            sb2.append( "Report file.\r\n" );
            sb2.append( longName );
            sb2.append( "\r\n" );
            sb2.append( vendorVersion );
            sb2.append( "\r\n\r\n" );
            StringBuilder sb3 = new StringBuilder( "" );
            // make and save report
            int n = atma1.length;
            for ( int i=0; i<n; i++ )
                {
                if ( atma1[i] != null ) 
                    { 
                    sb3.append( tableReport( atma1[i] ) );
                    sb3.append( "\r\n\r\n" );
                    }
                if ( atma2[i] != null ) 
                    { 
                    sb3.append( tableReport( atma2[i]) );
                    sb3.append( "\r\n" );
                    }
                }
            sb2.append( sb3 );
            saveReport( parentWin, s1, sb2.toString() );
            // after save report
            inDialogue = false;
            }  else { inDialogue = false; }
        }    // End of save dialogue cycle
    }        // End of method

// Helper method for convert table model to string
private static String tableReport ( AbstractTableModel atm ) {
    StringBuilder report = new StringBuilder( "" );
    if ( atm == null )
        {
        return report.toString(); 
        }
    // Continue if table exist,get geometry
    int m = atm.getColumnCount();
    int n = atm.getRowCount();
    String s;
    int a;
    int[] maxcols = new int[m];
    int maxcol = MAXCOL_DEFAULT;
    // Get column names lengths
    for ( int i=0; i<m; i++ )
        { 
        maxcols[i] = atm.getColumnName(i).length(); 
        }
    // Get column maximum lengths
    for (int j=0; j<n; j++)
        {
        for ( int i=0; i<m; i++ )
            {
            s = getShortString( atm, j, i );
            a = s.length();
            if ( a>maxcols[i] ) 
                { 
                maxcols[i]=a; 
                }
            }
        }
    for ( int i=0; i<maxcols.length; i++ ) { maxcol += maxcols[i]; }
    // Write table up
    for ( int i=0; i<m; i++ )
        {
        s = atm.getColumnName( i );
        report.append( " " );
        report.append( s );
        a = maxcols[i] - s.length() + 1;
        for ( int k=0; k<a; k++ )
            { 
            report.append( " " );
            }
        }
    // Write horizontal line
    report.append( "\r\n" );
    for ( int i=0; i<maxcol; i++ )
        { 
        report.append( "-" );
        }
    report.append( "\r\n" );
    // Write table content
    for ( int j=0; j<n; j++ )       // this cycle for rows , n = rows count
        {
        for ( int i=0; i<m; i++ )   // this cycle for columns , m = columns count
            {
            s = getShortString( atm, j, i );
            report.append( s );
            a = maxcols[i] - s.length() + 2;
            for ( int k=0; k<a; k++ ) 
                {
                report.append( " " );
                }
            }
            report.append( "\r\n" );
        }
    // Write horizontal line
    for ( int i=0; i<maxcol; i++ ) 
        { 
        report.append( "-" );
        }
    report.append ("\r\n" );
    // Return
    return report.toString();
    }

// Helper method for get short version of strings for report
private static String getShortString ( AbstractTableModel atm, int j, int i )
    {
    String s1 = " " + (String)atm.getValueAt( j, i );
    StringBuilder sb1 = new StringBuilder( s1 );
    int n = MAXCOL_LIMIT;
    int m = n-3;
    if ( s1.length() > n ) 
        {
        sb1 = new StringBuilder( "" );
        for ( int k=0; k<m; k++ )
            { 
            sb1.append( s1.charAt(k) );
            }
        sb1.append( "..." );
        }
    return sb1.toString();
    }

// Helper method for save string to file and visual status
private static void saveReport
                   ( JFrame parentWin, String filePath, String fileData ) 
    {
    int status=0;
    try ( FileWriter writer = new FileWriter( filePath, false ) )
        { writer.write( fileData ); writer.flush(); }
    catch( Exception e ) 
        { 
        status=1; 
        }
            
    if ( status == 0 )
        {
        JOptionPane.showMessageDialog
            ( parentWin, "Report saved: " + filePath, "REPORT",
              JOptionPane.WARNING_MESSAGE ); 
        }
    else
        {
        JOptionPane.showMessageDialog
            ( parentWin, "Write report failed", "ERROR",
              JOptionPane.ERROR_MESSAGE ); 
        }
    }
}

