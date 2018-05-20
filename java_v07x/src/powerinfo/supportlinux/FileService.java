//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Builder for linux system tree by virtual file systems =============

package powerinfo.supportlinux;

import java.io.*;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import powerinfo.*;

public class FileService
{
private final static int BUFFER_SIZE = 4096;
private final static int DEFAULT_RECURSION_LIMIT = 8;

public static void builtTree
    ( ArrayList<DefaultMutableTreeNode> treeArray , 
      int recursion , DefaultMutableTreeNode selected , 
      boolean enableDirs , boolean enableFiles )
    {
//--- Start cycle for recursive handling ---
    int m;
    if (recursion<0) { m = DEFAULT_RECURSION_LIMIT; }
    else             { m = recursion; }
    for ( int k=0; k<m; k++ )
        {
        boolean request = false;
//--- Start cycle for entries ---
        int n = treeArray.size();
        for ( int i=0; i<n; i++ )
            {
            DefaultMutableTreeNode x1 = treeArray.get(i);
//--- Support selective mode, skip if not selected ---
            if ( (selected != null) & ( selected != x1 ) ) continue;
//--- Analyse current entry, skip if already handled ---
            ListEntry x2 = (ListEntry)x1.getUserObject();
            if (x2.handled==true) continue;
            request = true;
            x2.failed = false;
//--- Start handling current entry ---        
            String x3 = x2.path;
            File file1 = new File(x3);
            boolean exists1 = file1.exists();
            boolean directory1=false;
            if (exists1) { directory1 = file1.isDirectory(); }
//--- Handling directory: make list of childs directories/files ---
        if ( exists1 & directory1 )
            {
            String[] list = file1.list();
            int count=0;
            if (list!=null) count = list.length;
            for ( int j=0; j<count; j++ )
                {
                String s1 = list[j];
                String s2 = x3+"/"+s1;
                File file2 = new File(s2);
                boolean dir = file2.isDirectory();
                if ( ( enableDirs & dir ) | ( enableFiles & !dir ) )
                    {
                    ListEntry y1 = 
                        new ListEntry ( s1, "", s2, false, false );
                    DefaultMutableTreeNode y2 = 
                        new DefaultMutableTreeNode(y1);
                    treeArray.add(y2);
                    x1.add(y2);
                    x1.setAllowsChildren(true);     // this entry is DIRECTORY
                    }
                }
            }
//--- Handling file: read content ---
        if ( exists1 & !directory1 )
            {
            int readSize = 0;
            //
            StringBuilder data = new StringBuilder("");
            FileInputStream fis = null;
            byte[] array = new byte[BUFFER_SIZE];
            try 
                { 
                fis = new FileInputStream(file1);
                readSize = fis.read(array); 
                fis.close();
                }
            catch (Exception e) 
                   // { data = "N/A : " + e; x2.failed=true; }
                   { data.append("N/A : " + e); x2.failed=true; }
            char c1;
            for (int j=0; j<readSize; j++)
                { 
                c1 = (char)array[j];
                // if ( (c1=='\n') | (c1=='\r') ) { data = data + "  "; }
                if ( (c1=='\n') | (c1=='\r') ) { data.append("  "); }
                else 
                    { 
                    if ((c1<' ')|(c1>'z')) { c1 = '_'; }
                    // data = data + c1;
                    data.append(c1);
                    }
                }
                x2.name2 = data.toString();
                x2.leaf = true;
            x1.setAllowsChildren(false);  // this entry is FILE
            }
//--- End cycle for entries ---
            x2.handled = true;
            }
//--- End cycle for recursion ---
        if (request==false) break;
        }
    }

public static String readParameter( String s1 )
    {
    StringBuilder s2=null;
    File file1 = new File(s1);
    boolean exists1 = file1.exists();
    boolean directory1 = false;
    if (exists1) { directory1 = file1.isDirectory(); }
    if ( exists1 & ! directory1 )
        {
        FileInputStream fis = null;
        int readSize = 0;
        byte[] array = new byte[BUFFER_SIZE];
        try 
            { 
            fis = new FileInputStream(file1);
            readSize = fis.read(array); 
            fis.close();
            }
            catch (Exception e) { readSize=0; }
            char c1;
            if ( readSize>0 )
                {
                s2 = new StringBuilder("");
                for (int j=0; j<readSize; j++)
                    { 
                    c1 = (char)array[j];
                    // if ( ! ( (c1=='\n') | (c1=='\r') ) ) { s2 = s2 + c1; }
                    if ( ! ( (c1=='\n') | (c1=='\r') ) ) { s2.append(c1); }
                    }
                }
        }
    if (s2==null) return null;
    return s2.toString();
    }
    
}

