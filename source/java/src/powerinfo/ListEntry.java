/*
Java Power Info utility, (C)2021 IC Book Labs
Entry of system information trees
*/

package powerinfo;

/*
strings notes:
name1 = item name
name2 = item value, can be visualized as bold or colored
path = application-specific path of node, example: file path 

boolean flags notes:
handled = if true, means this entry executed, for example opened node
leaf = true for leafs (files) , false for openable nodes (directories)
       for some applications redundant with setAllowsChildren()
failed = true if error detected
*/

public class ListEntry
    { 
    public String name1, name2, path;
    public boolean handled, leaf, failed; 
    public ListEntry( String s1, String s2, String s3, boolean b1, boolean b2 )  
        { name1 = s1; name2 = s2; path = s3; handled = b1; leaf = b2; }
    @Override public String toString()
        { 
        if ( leaf == false ) 
            { return name1; }
        else if ( failed == false ) 
            { return "<html>" + name1 + " = <b><font color=blue>" + name2; }
        else
            { return "<html>" + name1 + " = <b><font color=gray>" + name2; }
        }
    }
