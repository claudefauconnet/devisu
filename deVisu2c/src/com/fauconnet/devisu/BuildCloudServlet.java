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
package com.fauconnet.devisu;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fauconnet.tagcloud.CloudProcessor;

/**
 * Servlet implementation class buildServlet
 */
public class BuildCloudServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public BuildCloudServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doRequest(request, response);

	}

	private void doRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=ISO-8859-1");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", -1);
		HttpSession session = request.getSession();

		// affichage du select contenant les tags par ordre de frequence
		// décroissant (pour chart)
		String tagListSelect = request.getParameter("tagListSelect");
		if (tagListSelect != null && tagListSelect.equals("true")) {
			String str = null;
			str = (String) session.getAttribute("tagListSelect");
			session.removeAttribute("tagListSelect");
			if (str != null) {
				Writer writer = response.getWriter();
				writer.write(str);
				writer.close();
			}
			return;

		}
		String resetCache = request.getParameter("resetCache");
		if (resetCache != null && resetCache.equals("true")) {
			getDisplayer(session, request).resetCache();
			return;
		}

		// génération du TagCloud et de la selectList

		String categoryId = request.getParameter("categoryId");
		String durationStr = request.getParameter("duration");
		String fromStr = request.getParameter("from");
		String filter = request.getParameter("filter");
		String nTags = request.getParameter("nTags");
		String chartTags = request.getParameter("chartTags");
		if (nTags == null || nTags.equals(0) || nTags.equals("") || nTags.equals("undefined")) {
			nTags = "70";
		}

		// demande du tagCloud
		if (durationStr != null && durationStr.length() > 0) {
			String str = "";
			int duration = Integer.parseInt(durationStr) * -1;
			Displayer displayer = getDisplayer(request.getSession(true), request);
			this.getServletContext().setAttribute("RssDisplayer", displayer);
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.add(Calendar.DATE, 1);// on se situe demain à 0h
			Date date1 = calendar.getTime();
			int from = 0;
			if (fromStr != null && fromStr.length() > 0) {
				from = Integer.parseInt(fromStr) * -1;
				calendar.add(Calendar.DATE, from);
				date1 = calendar.getTime();
			}
			calendar.add(Calendar.DATE, duration);
			Date date2 = calendar.getTime();
			String output = request.getParameter("output");
			if (output == null)
				output = "cloud";
			// on force le passage par le filtre pour isCloudWithTitleOnly :
			// recalcul du cloudtag
			// if(displayer.isCloudWithTitleOnly()&& (filter == null ||
			// filter.equals(""))){
			if ((filter == null || filter.equals(""))) {
				filter = "|";
			}
			if (output.equals("cloud")) {
				if (filter != null && filter.length() > 0) {
					str = displayer.getFilteredTagsCloudBetweenDates(session, filter, date2, date1, Integer.parseInt(nTags));
					String str2 = displayer.getHtmlTagsListBetweenDates(session, categoryId, date2, date1);
					session.setAttribute("tagListSelect", str2);
				} else {
					str = displayer.getTagsCloudBetweenDates(session, categoryId, date2, date1);
					String str2 = displayer.getHtmlTagsListBetweenDates(session, categoryId, date2, date1);
					session.setAttribute("tagListSelect", str2);
				}
			}
			// comparaison de deux annÈes
			else if (output.equals("compareCloud")) {
				String nCompareTagsStr = request.getParameter("nCompareTags");
				int nCompareTags;
				if (nCompareTagsStr == null || nCompareTagsStr.equals("") || nCompareTagsStr.equals("undefined"))
					nCompareTags = 300;
				else
					nCompareTags = Integer.parseInt(nCompareTagsStr);
				String compareCategory = request.getParameter("compareCategory");
				if (compareCategory != null && !compareCategory.equals("")) {
					int compareCategoryNum = Integer.parseInt(compareCategory);

					str = displayer.getFilteredTagsCloudComparisonBetweenDates(session, compareCategoryNum, filter, false, date2, date1, nCompareTags);
					String str2 = displayer.getHtmlTagsListBetweenDates(session, categoryId, date2, date1);
					session.setAttribute("tagListSelect", str2);
				}

			} else if (output.equals("treeMap")) {
				if (filter.length() == 0)
					filter = null;
				TagTreeGenerator tagTreeGenerator = new TagTreeGenerator();
				str = tagTreeGenerator.getTagTreeMap(session, filter, 15, 2, date2, date1);
			} else if (output.equals("spaceTree")) {
				if (filter.length() == 0)
					filter = null;
				TagTreeGenerator tagTreeGenerator = new TagTreeGenerator();
				str = tagTreeGenerator.getTagTree(session, 10, filter, date2, date1);

				// demande de l'historyChart

			} else if (output.equals("buildChart") && chartTags != null && chartTags.length() > 0) {
				str = "";
				boolean perctBool = false;
				String percentages = request.getParameter("percentages");
				if (percentages != null && percentages.equals("true")) {
					perctBool = true;
				}
				if (filter != null && filter.length() == 0) {
					filter = null;
				}
				Displayer displayer2 = new Displayer();
				ClickableChart clickableChart = displayer2.getTagHistoryImage(filter, chartTags, date2, date1, perctBool);
				session.setAttribute("clickableChart", clickableChart);
				// une variable aléatoire pour assurer le rechargement de l'image
				// en
				// Ajax
				str = "<img src='buildCloud?var=" + Math.random() + "&loadChart=true'usemap='#chart' >\n";
				str += clickableChart.getImageMap();
			}
			Writer writer = response.getWriter();
			writer.write(str);
			writer.close();
			return;

		}

		String loadChart = request.getParameter("loadChart");
		if (loadChart != null && loadChart.length() > 0) {
			ClickableChart clickableChart = (ClickableChart) session.getAttribute("clickableChart");
			if (clickableChart != null) {

				// Displayer displayer=new Displayer();

				// byte[] bytes=displayer.getTagHistoryImage( name, null, null);
				response.setContentType("image/png");
				OutputStream os = response.getOutputStream();
				os.write(clickableChart.getImage());
				os.close();
				session.removeAttribute("clickableChart");
				return;

			}
		}
		String loadCloudImage = request.getParameter("loadCloudImage");
		if (loadCloudImage != null && loadCloudImage.length() > 0) {
			byte[] cloudeImageBytes = (byte[]) session.getAttribute("cloudImage");
			if (cloudeImageBytes != null) {

				// Displayer displayer=new Displayer();

				// byte[] bytes=displayer.getTagHistoryImage( name, null, null);
				response.setContentType("image/png");
				OutputStream os = response.getOutputStream();
				os.write(cloudeImageBytes);
				os.close();
				session.removeAttribute("cloudImage");
				return;

			}
		}
		
		

		String autoComplete = request.getParameter("autoComplete");
		if (autoComplete != null && autoComplete.equals("true")) {
			String val = request.getParameter("val");
			Writer writer = response.getWriter();
			if (val == null || val.length() == 0) {
				writer.write("");
			} else {
				// String str="['aaa','bbb','ccc']";
				String str = getDisplayer(session, request).getAutocompletionJavascriptArray(val);
				writer.write(str);
			}
			writer.close();
		}
	}

	private Displayer getDisplayer(HttpSession session, HttpServletRequest request) {
		Displayer displayer = (Displayer) session.getAttribute("displayer");
		if (displayer == null) {
			displayer = new Displayer();
			session.setAttribute("displayer", displayer);
		}
		String titleOnly = request.getParameter("titleOnly");
		if (titleOnly != null) {
			if (titleOnly.equals("true"))
				displayer.setCloudWithTitleOnly(true);
			else
				displayer.setCloudWithTitleOnly(false);
		}
		String withCoupledTags = request.getParameter("withCoupledTags");
		if (withCoupledTags != null) {
			if (withCoupledTags.equals("true"))
				displayer.setWithCoupledTags(true);
			else
				displayer.setWithCoupledTags(false);
		}

		String orderAlpha = request.getParameter("orderAlpha");
		if (orderAlpha != null) {
			if (orderAlpha.equals("true"))
				displayer.setOrderAlpha(true);
			else
				displayer.setOrderAlpha(false);
		}
		String categoryId = request.getParameter("categoryId");
		if (categoryId != null) {
			displayer.setCurrentCategory(Integer.parseInt(categoryId));

		}

		return displayer;
	}

}
