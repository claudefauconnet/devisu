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

public class Tag implements Comparable {
	private int weight;
	private String link;
	private String name;
	private int frequency;

	public Tag() {
		// TODO Auto-generated constructor stub
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public Tag(int weight, int frequency, String link, String name) {
		this.weight = weight;
		this.link = link;
		this.name = name;
		this.frequency = frequency;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return name + "  " + weight;
	}

	@Override
	public int compareTo(Object o) {
		Tag tag2 = (Tag) o;
		if (this.weight > (tag2.getWeight()))
			return 1;
		else
			return -1;
	}

}
