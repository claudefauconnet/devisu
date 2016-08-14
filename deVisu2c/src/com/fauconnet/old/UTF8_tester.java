package com.fauconnet.old;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class UTF8_tester {

	public static void main(String[] args) {
		String filePath="C:\\Local\\rss3d\\importMongo08_14.csv";
		String filePath2="C:\\Local\\rss3d\\importMongo08_14_2.csv";
		System.out.println("aaa");
		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "ISO-8859-1"));
			bw= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath2), "UTF-8"));
	
		String line;
		String result="";
		int i=0; 
		while((line=br.readLine())!=null){
			bw.write(line+"\n");
		/*List list=getNonUnicodeCharacters(line);
		i++;
		if(list.size()>0)
			System.out.println(""+i+list);
		result+=list.toString();*/
		}
		br.close();
		bw.close();
		
		System.out.println ("--done--");
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	
	protected static  List< String > getNonUnicodeCharacters( String s ) {
		  final List< String > result = new ArrayList< String >();
		  for ( int i = 0 , n = s.length() ; i < n ; i++ ) {
		    final String character = s.substring( i , i + 1 );
		    final boolean isOtherSymbol = 
		      ( int ) Character.OTHER_SYMBOL
		       == Character.getType( character.charAt( 0 ) );
		    final boolean isNonUnicode = isOtherSymbol 
		      && character.getBytes()[ 0 ] == ( byte ) 63;
		    if ( isNonUnicode )
		      result.add( character+ "   "+s );
		  }
		  return result;
		}
}
