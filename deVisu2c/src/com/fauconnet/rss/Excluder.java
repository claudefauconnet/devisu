/****************************************************************************
 *                               RSS3D                            *
 ****************************************************************************
 * Copyright (C) 06/2002
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact the Author:
 *
 * Claude Fauconnet
 * France
 * mailto: claude.fauconnet@neuf.fr
 *
 ***************************************************************************/
package com.fauconnet.rss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Excluder {
	private String excludedWords;
	public static String DICTIONARY_NAME = "excludeDictionary.txt";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public Excluder() {

		excludedWords = loadDictionnary();
		/*
		 * excludedWords =
		 * "le,la,les,un,une, des,je,tu,il,elle,nous,vous,ils,moi,toi,lui,eux,a,est,sont"
		 * ;excludedWords+=
		 * "si,et,pour,à,dans,du,en,par,sur,être,que,au,qui,plus,pas,ce,avec";
		 * excludedWords+=
		 * "aux,au,cette,ce,ces, non, oui,donc,d'un,d'une,ceci,c'est,très,d'un,d'une"
		 * ;excludedWords+=
		 * "fait, entre, trois,très,après,avant,pendant,été,moins, contre, dont,ses,sous,son"
		 * ; excludedWords+="tout,car,cela,comme,bien,mais,tout,rien,trop";
		 * excludedWords+="veut,deux";
		 * excludedWords+="notre, nos,votre, vos, leurs, leur,via";
		 * 
		 * excludedWords+=
		 * "the,and,or,for,between,by,in,how,is,are,have,you,your, me, them,else,not"
		 * ;excludedWords+=
		 * "the, one, for I, you, he, she, we, you, they, me, you, him, them, was, is, are"
		 * ; excludedWords +=
		 * "if,and,to,at,in,of,by,for,on,be ,that,in,which,more,no,what,with ";
		 * excludedWords +=" to,this,that,therefore";
		 */
	}

	public String getExcludedWords() {
		return excludedWords;
	}

	public void setExcludedWords(String excludedWords) {
		this.excludedWords = excludedWords;
	}

	private String loadDictionnary() {
		try {

			StringBuffer sb = new StringBuffer();
			String path =  com.fauconnet.rss.Resources.class.getResource(DICTIONARY_NAME).getPath();
			File file = new File(path);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = reader.readLine()) != null) {

				if (line.length() > 0 && line.charAt(0) != '#') {
					sb.append(line);
				}
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";

		}

	}

}
