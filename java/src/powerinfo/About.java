/*
Java Power Info utility, (C)2018 IC Book Labs
Strings with getters for product and vendor information
*/

package powerinfo;

public class About {

private final static String VERSION_NAME = "v0.83";
private final static String VENDOR_NAME  = "(C)2020 IC Book Labs";
private final static String SHORT_NAME   = "Power Info " + VERSION_NAME;
private final static String LONG_NAME    = "Java " + SHORT_NAME;
private final static String WEB_SITE     = "http://icbook.com.ua";
private final static String VENDOR_ICON  = "/powerinfo/resources/icbook.jpg";

public static String getVersionName() { return VERSION_NAME; }
public static String getVendorName()  { return VENDOR_NAME;  }
public static String getShortName()   { return SHORT_NAME;   }
public static String getLongName()    { return LONG_NAME;    }
public static String getWebSite()     { return WEB_SITE;     }
public static String getVendorIcon()  { return VENDOR_ICON;  }
    
}
