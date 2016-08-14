/****************************************************************************
 *                               RSS3D                            *
 ****************************************************************************
 * Copyright (C) 06/2002
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General private License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General private License for more details.
 *
 * You should have received a copy of the GNU General private License
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
package com.fauconnet.pot;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

import com.fauconnet.devisu.I_Processor;
import com.fauconnet.pptx.PptxBuilderPOI;
import com.fauconnet.rss.Excluder;
import com.fauconnet.rss.Tag;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class UseCasesProcessor implements I_Processor {
	private int maxTags = 70;
	private int classesNb = 6;
	private Hashtable words;
	private String excludedWords;
	private int max;

	@SuppressWarnings("unchecked")
	@Override
	public Object process(Map params) {
		List<DBObject> items = (List<DBObject>) params.get("data");
		int nFeeds=items.size();
		String method = (String) params.get("method");
		String maxTagsStr = (String) params.get("maxTags");
		if(maxTagsStr!=null){
			maxTags=Integer.parseInt(maxTagsStr);
		}
		if (method == null) {
			return new BasicDBObject("Error", " no method declared").toString();
		}
		
		
		
		if (method.equals("getTags")) {
			StringBuffer text = new StringBuffer();
			for (DBObject item : items) {
				text.append(item.get("name") + "\n");
				//text.append(item.get("description") + "\n");
			}
			List<Tag> tags = getTags(text.toString(), false);

			BasicDBList jsonTags = new BasicDBList();
			for (Tag tag : tags) {
				DBObject jsonTag = new BasicDBObject();
				jsonTag.put("key", tag.getName());
				jsonTag.put("value", tag.getWeight());
				jsonTags.add(jsonTag);
			}
			return jsonTags.toString();
		}
		
		
		if (method.equals("getUseCasesPowerpoint")) {
			String fileName="useCases.pptx";
			String dataDirPath=(String)params.get("dataDirPath");
			String targetPath=dataDirPath+"/"+fileName;
			PptxBuilderPOI pptxBuilder=new PptxBuilderPOI();
			BasicDBObject query=(BasicDBObject)params.get("query");
			
			XMLSlideShow slideShow=pptxBuilder.createUseCasesPowerpoint(PptxBuilderPOI.templateUseCasesPath, query);
			try {
				pptxBuilder.saveXMLSlideShowAsFile(slideShow, targetPath);
			//	return "[{url:\"data/"+fileName+"\"}]";
				return "{\"url\":\"data/"+fileName+"\"}";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
				System.out.println("Presentation edited successfully");
		}
		
		
		if (method.equals("getLinks")) {
			return items.toString();
		}
		return new BasicDBObject("Error", " ...").toString();
	}

	private int getMaxTags() {
		return maxTags;
	}

	private void setMaxTags(int maxTags) {
		this.maxTags = maxTags;
	}

	private int getMax() {
		return max;
	}

	private void setMax(int max) {
		this.max = max;
	}

	public UseCasesProcessor(int maxTags, int classesNb, String excludedWords) {
		this.maxTags = maxTags;
		this.classesNb = classesNb;
		this.excludedWords = excludedWords;
		init();
	}

	public UseCasesProcessor() {
		init();
	}

	private void init() {
		words = new Hashtable();
		if (excludedWords == null)
			excludedWords = new Excluder().getExcludedWords();
	}

	private Hashtable getWords() {
		return words;
	}

	private void setWords(Hashtable words) {
		this.words = words;
	}

	private List<Tag> getTagsForStorage(String text) {
		return getTagsForStorage(text, false);

	}

	/*
	 * retourne une liste de Mytag brute tronqu?e mais sans weight et sans tri
	 */
	private List<Tag> getTagsForStorage(String text, boolean orderAlpha) {
		if (text != null)
			parseText(text);
		List<Tag> tags = new ArrayList<Tag>();
		// remplissage de la Liste des Tags
		Enumeration e = words.keys();
		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			String name = (String) key;
			int frequency = ((Integer) words.get(key)).intValue();

			Tag tag = new Tag(frequency, frequency, name, name);
			tags.add(tag);

		}
		words = null;

		removePlurals(tags);
		sortByFrequencyDesc(tags);
		reduceToMaxNumbOfTags(tags);
		if (orderAlpha)
			sortTagNamesAsc(tags);
		return tags;

	}

	/*
	 * 
	 * retourne une liste de MyTag (composée) complete et tri?e
	 */
	private List<Tag> getTags(String text) {
		return getTags(text, false);
	}

	/*
	 * 
	 * retourne une liste de MyTag complete et tri?e withCoupledTags indique si
	 * on calcule les composition de Tags (couples)
	 */
	private List<Tag> getTags(String text, boolean orderAlpha) {
		if (text != null)
			parseText(text);
		List<Tag> tags = new ArrayList<Tag>();
		// remplissage de la Liste des Tags
		Enumeration e = words.keys();
		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			String name = (String) key;
			int frequency = ((Integer) words.get(key)).intValue();

			Tag tag = new Tag(frequency, frequency, name, name);
			tags.add(tag);

		}
		words = null;

		removePlurals(tags);
		sortByFrequencyDesc(tags);
		reduceToMaxNumbOfTags(tags);
		allocateClasses(tags);
		if (orderAlpha)
			sortTagNamesAsc(tags);
		return tags;

	}

	private void removePlurals(List<Tag> tags) {
		ListIterator li = tags.listIterator();
		List<Tag> plurals = new ArrayList<Tag>();
		while (li.hasNext()) {
			Tag tag = (Tag) li.next();
			String word = tag.getName();
			if (word.endsWith("s") || word.endsWith("x")) {
				word = word.substring(0, word.length() - 1);
				ListIterator li2 = tags.listIterator();
				while (li2.hasNext()) {
					Tag tag2 = (Tag) li2.next();
					if (tag2.getName().indexOf(word) > -1) {
						plurals.add(tag);
						break;
					}
				}

			}

		}
		tags.removeAll(plurals);

	}

	/*
	 * renvoie la liste des tags a appeler apr?s un ou plusieurs appels de
	 * parseText()
	 */
	private List<Tag> getTags() {

		return getTags(null);

	}

	// tri décroissant de la liste des tags
	@SuppressWarnings("unchecked")
	public void sortByFrequencyDesc(List<Tag> tags) {
		// tri
		Collections.sort(tags, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {

				double w1 = ((Tag) o1).getFrequency();
				double w2 = ((Tag) o2).getFrequency();
				if (w1 < w2)
					return 1;
				else if (w1 > w2)
					return -1;
				else
					return 0;
			};

		});

	}

	// tri décroissant de la liste des tags
	@SuppressWarnings("unchecked")
	public void sortByFrequencyAsc(List tags) {
		// tri
		Collections.sort(tags, new Comparator() {
			public int compare(Object o1, Object o2) {
				double w1 = ((Tag) o1).getFrequency();
				double w2 = ((Tag) o2).getFrequency();
				if (w1 >= w2)
					return 1;
				else
					return -1;

			}
		});

	}

	/*
	 * reduction
	 */
	private void reduceToMaxNumbOfTags(List tags) {
		while (tags.size() > maxTags) {
			tags.remove(tags.size() - 1);
		}

	}

	/*
	 * affecte un poids à chauqe tag en fonction de sa frequence on part de la
	 * plus grande classe dans laquelle on met les deux plus frquents éléments
	 * puis on double le noombre d'elements à chaque classe
	 */
	private void allocateClasses(List tags) {

		int[] distrib = new int[classesNb];
		int n = tags.size();
		int incr = 2;
		incr = (int) Math.round(((double) n) / 50) + 1;
		for (int i = 0; i < classesNb; i++) {
			distrib[i] = (classesNb - i) * incr;
		}
		distrib[0] = 999999999;

		ListIterator li = tags.listIterator();
		int currentClass = classesNb - 1;

		while (li.hasNext()) {
			int count = 0;

			while (count < distrib[currentClass] && li.hasNext()) {
				count += 1;
				Tag tag = (Tag) li.next();
				tag.setWeight(currentClass + 1);
			}
			currentClass -= 1;

		}

		/*
		 * MyTag minTag=(MyTag)Collections.min(tags); double min =
		 * (double)minTag.getFrequency(); ClassBuilder classbuilder=new
		 * ClassBuilder(classesNb, logDistrib, min, max); ListIterator li =
		 * tags.listIterator(); while (li.hasNext()) { MyTag tag = (MyTag)
		 * li.next(); int weight=classbuilder.affectClass(tag.getFrequency());
		 * //System.out.println(tag.toString()+" / "+weight);
		 * tag.setWeight(weight); }
		 */
	}

	private int[] getClassesDistribution(List tags) {
		int[] distrib = new int[classesNb];

		ListIterator li = tags.listIterator();
		while (li.hasNext()) {
			Tag tag = (Tag) li.next();
			int classNum = tag.getWeight();
			distrib[classNum - 1] += 1;

		}

		return distrib;

	}

	/*
	 * tri par tag name desc
	 */
	public void sortTagNamesAsc(List tags) {
		Collections.sort(tags, new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = ((Tag) o1).getName();
				String s2 = ((Tag) o2).getName();
				return s1.compareToIgnoreCase(s2);

			}
		});
	}

	private void parseText(String str) {

		String[] result = str.split("[/()\\s.,;Õ:'ÇÈ\"]");
		for (int x = 0; x < result.length; x++) {
			String token = result[x].toLowerCase();
			boolean objectExists = false;
			if (selectWord(token)) {
				Object obj = words.get(token);
				if (obj == null) {
					words.put(token, new Integer(1));
				} else {
					int frequency = ((Integer) obj).intValue();
					words.put(token, new Integer(frequency + 1));
				}
			}

		}

	}

	/*
	 * private Map sortByValue(Map map) { List list = new
	 * LinkedList(map.entrySet()); Collections.sort(list, new Comparator() {
	 * private int compare(Object o1, Object o2) { return ((Comparable)
	 * ((Map.Entry) (o1)).getValue()) .compareTo(((Map.Entry) (o2)).getValue())
	 * -1; } }); // logger.info(list); Map result = new LinkedHashMap(); for
	 * (Iterator it = list.iterator(); it.hasNext();) { Map.Entry entry =
	 * (Map.Entry) it.next(); result.put(entry.getKey(), entry.getValue()); }
	 * return result; }
	 */

	private boolean selectWord(String word) {
		if (word.length() < 3 || excludedWords.indexOf(word.toLowerCase()) > 0)
			return false;
		return true;
	}

	private static String GetArrayString(int[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sb.append("[" + i + "] : " + array[i] + "\n");
		}
		return sb.toString();
	}

	/**
	 * @param args
	 */
	private static void main(String[] args) {
		// TODO Auto-generated method stub

		String text = " mon oncle est arrivé. Il est venu mon oncle chez moi. Moi je le trouve gran mon oncle, très grand";
		UseCasesProcessor cloud = new UseCasesProcessor();

		// System.out.println(cloud.getWords().toString());
		cloud.getTags(text);
	}

}
