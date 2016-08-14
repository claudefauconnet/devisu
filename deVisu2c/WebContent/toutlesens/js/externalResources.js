
/**************************  TOUTLESENS LICENCE*************************

The MIT License (MIT)

Copyright (c) 2016 Claude Fauconnet claude.fauconnet@neuf.fr

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

**********************************************************************/

/*restWord
 * 
 * 
 * http://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&query=select*{
 * http://dbpedia.org/page/Los_Angeles rdfs:label ?label}&format=json
 * 
 * 
 * http://fr.slideshare.net/paolocristofaro1/virtuoso-rdf-triple-store-analysis-benchmark-mapping-tools-rdf-oo
 * 
 * http://en.lodlive.it/?http://dbpedia.org/resource/Troie
 * 
 * http://fr.dbpedia.org/resource/
 * 
 */


var externalResourcesOn=false;
function getDbpediaInfo(word) {

	executeRestQuery("restapi", getDbpediaInfoCallback);

}

function generateExternalImg(src) {
	var imgWidth = $("#imagePanel").width();
	var imgHeight = $("#imagePanel").height();
	var imgSize;
	if (imgWidth > imgHeight)
		imgSize = "height='" + imgHeight + "'";
	else
		imgSize = "width='" + imgWidth + "'";

	var strImage = "<a href='" + src + "' target='blank_'><img src='" + src
			+ "' " + imgSize + " align='center'></a>";
	$("#imagePanel").html(strImage);
}

function getDbpediaInfoCallback(result) {

	var data = result.d.results[0];
	var i = 0;
	var str = "";
	var cleanedData = {}

	for ( var key in data) {

		var keyLabel = key.substring(key.lastIndexOf("/") + 1);

		var value = data[key];
		if (!$.isPlainObject(value)) {// string

			cleanedData[keyLabel] = keyLabel + "-->" + value;

			str += keyLabel + "....." + JSON.stringify(cleanedData[keyLabel])
					+ "<br>";
			// console.log(keyLabel);http://dbpedia.org/spar
		} else {// object deffered

			var value = value["__deferred"];
			if (value) {
				cleanedData[keyLabel] = keyLabel + "-->"
						+ JSON.stringify(value);

				str += keyLabel + "....."
						+ JSON.stringify(cleanedData[keyLabel]) + "<br>";
				// console.log(keyLabel);

			} else {
				var xxx = value;
				cleanedData[keyLabel] = keyLabel + "--?->"
						+ JSON.stringify(value);
			}

		}

		str += keyLabel + "....." + JSON.stringify(cleanedData[keyLabel])
				+ "<br>";

		function getDbpediaInfoCallback(result) {
		}
		// var strOld = $("#infoPanel").html();
		$("#infoPanel").html(+strOldstr);
	}
}

function executeRestQuery(url, callback) {
	word = "Rome";
	word = $("#restWord").val();
	concept = $("#concept").val();
	langage = $("#langage").val();
	// var query = "http://dbpedia.org/data/" + word + ".json";
	var query = getUrlDbpediaConcept(word, concept, langage)

	var payload = {
		mode : "GET",
		url : query,

	}

	$.ajax({
		type : "POST",
		url : "restapi",
		data : payload,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if (callback) {
				callback(data);
			}

		},

		error : function(xhr, err, msg) {
			console.log(xhr);
			console.log(err);
			console.log(msg);
		}

	});
}

function getUrlDbpediaAbstract(word) {

	$format = 'json';

	$query = "PREFIX dbp: <http://dbpedia.org/resource/> PREFIX dbp2: <http://dbpedia.org/ontology/>";

	$query += "SELECT ?abstract WHERE {dbp:"
			+ word
			+ " dbp2:abstract ?abstract .  FILTER langMatches(lang(?abstract), 'en')}";

	$searchUrl = "http://dbpedia.org/sparql?query="
			+ encodeURIComponent($query);
	// + "&format=.jon";
	return $searchUrl;
	var str = "";
}

function getUrlDbpediaConcept(word, concept, locale) {

	$format = 'json';

	/*
	 * $query = "PREFIX dbp: <http://dbpedia.org/resource/> PREFIX dbp2:
	 * <http://dbpedia.org/ontology/>";
	 * 
	 * $query += "SELECT ?"+concept+" WHERE {dbp:" + word + " dbp2:"+concept+"
	 * ?"+concept+" . FILTER langMatches(lang(?"+concept+"), '"+langage+"')}";
	 * 
	 * 
	 * $query="SELECT ?uri ?label WHERE {?uri rdfs:label ?label
	 * .filter(?label='"+word+"'@en)}";
	 * 
	 * $query="SELECT DISTINCT ?concept WHERE { ?s a ?concept .} LIMIT 50"
	 * 
	 * $query="SELECT DISTINCT ?label WHERE { ?s a ?label .} LIMIT 50"
	 * 
	 * //http://dbpedia.org/sparql?query=PREFIX%20dbp%3A%20%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2F%3E%20PREFIX%20dbp2%3A%20%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3ESELECT%20%3Fabstract%20WHERE%20%7Bdbp%3ARome%20dbp2%3Aabstract%20%3Fabstract%20.%20%20FILTER%20langMatches(lang(%3Fabstract)%2C%20%27en%27)%7D
	 * 
	 * 
	 * 
	 * $query = "select distinct ?property whe."re {?instance a
	 * <http://dbpedia.org/ontology/Pers_don> . ?instance ?property ?obj . }
	 * LIMIT 50"; // $query = "select distinct ?property where {?instance a
	 * <http://dbpedia.org/ontology/Person> . ?instance ?property ?obj
	 * .filter(?label='"+word+"'@en)} LIMIT 500"; //$query = "select distinct
	 * ?property where {?property <http://www.w3.org/2000/01/rdf-schema#domain>
	 * <http://dbpedia.org/ontology/Person> . }"; //$query = "select distinct
	 * ?property where {?property <http://www.w3.org/2000/01/rdf-schema#domain>
	 * <http://dbpedia.org/resource/Rome> . }"; //<http://dbpedia.org/resource/Barack_Obama>
	 * $searchUrl = "http://dbpedia.org/sparql?query=" +
	 * encodeURIComponent($query);
	 */

	var str = "";
	if (locale.length > 0)
		locale += ".";
	$query = "http://" + locale + "dbpedia.org/data/" + word + ".jsod";
	// http://fr.dbpedia.org/data/Zeus.jsodheight="50%">

	$("#urlPanel").html($query);

	return $query;
}

function getDbPediaNotice(obj) {
	if(!externalResourcesOn)
		return;
	$("#imagePanel").html("");
	
	var word = "";
	if (!obj)
		word = $("#restWord").val();

	/*
	 * WHERE { <http://dbpedia.org/resource/Arctic_Monkeys> dbpedia-owl:hometown
	 * ?c. }
	 */
	else {
		word = obj.nom;
	}
	if (obj.urlDbpedia) {
		word = obj.urlDbpedia;
		var p = word.lastIndexOf("/");
		if (p > 0) {
			word = word.substrng(p + 1);
		}
	}

	var type = obj.type;

	var props = {}

	props["Personne"] = [ "birthDate", "deathDate" ];
	var propsStr = [];
	if (type && props[type]) {
		for (var i = 0; i < props[type].length; i++) {
			var prop = props[type][i];
			propsStr += "prop:" + prop + " ?" + prop + ";"
		}
	}

	// reg ex ?x rdfs:label ?label FILTER regex(?label,'^péric','i')

	var optionalProps = "";
	optionalProps += " OPTIONAL { ?x prop:birthDate ?birthDate }\n";

	optionalProps += " OPTIONAL { ?x prop:deathDate ?deathDate }\n";
	optionalProps += "OPTIONAL { ?x prop:disciple ?disciple }\n";
	/*
	 * optionalProps += "OPTIONAL { ?x prop:philosophicalSchool
	 * ?philosophicalSchool }\n"; optionalProps += "OPTIONAL { ?x
	 * prop:activeYearsEndDateMgr ?activeYearsEndDateMgr }\n"; optionalProps +=
	 * "OPTIONAL { ?x prop:activeYearsEndYearMgr ?activeYearsEndYearMgr }\n";
	 * optionalProps += "OPTIONAL { ?x prop:activeYearsStartDateMgr
	 * ?activeYearsStartDateMgr }\n"; optionalProps += "OPTIONAL { ?x
	 * prop:activeYearsStartYearMgr ?activeYearsStartYearMgr}\n";
	 * //optionalProps += "OPTIONAL { ?x prop:startReign ?startReign}\n"; non
	 * populé
	 */
	optionalProps += "OPTIONAL { ?x prop:startDate ?startDate}\n";
	optionalProps += "OPTIONAL { ?x prop:endDate ?endDate}\n";
	optionalProps += "OPTIONAL { ?x prop:author ?author}\n";

	optionalProps += "OPTIONAL { ?x prop:originalTitle ?originalTitle}\n";
	optionalProps += "OPTIONAL { ?x prop:museum ?museum}\n";
	optionalProps += "OPTIONAL { ?x prop:subjectTerm ?subjectTerm}\n";

	var query = "PREFIX : <http://dbpedia.org/resource/>PREFIX dbpedia-owl:<http://dbpedia.org/ontology/> PREFIX prop: <http://dbpedia.org/property/>"
			+ "SELECT * WHERE {"
			+ "?x rdfs:label '"
			+ word
			+ "'@fr."
			+ optionalProps
			+ "?x <http://dbpedia.org/ontology/wikiPageID> ?id."
			+ "?x dbpedia-owl:abstract ?abstract."
			+ "?x dbpedia-owl:thumbnail ?thumbnail."

			+ "FILTER (LANG(?abstract)='fr')" + "}"

	//console.log(query);

	var query2 = "&format=json&timeout=30000";// &debug=on"
	$("#urlPanel").html(htmlEncode(query));
	console.log(query);
	var payload = "";// query;
	var url = "http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query="
			+ encodeURIComponent(query) + query2;

	paramsObj = {
		mode : "GET",
		// url:"http://dbpedia.org/sparql",
		url : url,
		urlSuffix : "",
		payload : JSON.stringify(payload)

	}

	$
			.ajax({
				type : "POST",
				url : "restapi",
				data : paramsObj,
				dataType : "json",
				success : function(_data, textStatus, jqXHR) {
					var obj = _data.results.bindings[0];
					var strImage;
					var str = "";

					if (!obj) {

					} else {
						str = " <br><b>Wikipedia : </font></b><br>";

						if (obj["thumbnail"]) {
							var src = obj["thumbnail"].value;
							generateExternalImg(src);

						}
						str += "<span>";
						// str+="<B>Wikipedia EN</B><br>";
						for ( var key in obj) {
							if (key == "id") {
								https: // en.wikipedia.org/?curid=308
								str += "<a target='blank_' href='http:///en.wikipedia.org/?curid="
										+ obj[key].value
										+ "'>lien Wikipedia Anglais</a><br>";
							} else if (key == "thumbnail") {
								;
							} else if (key == "x") {
								continue;

							} else if (key == "abstract") {
								continue;
							} else {
								var val = obj[key].value;
								var p=val.indexOf("http://");
								if (p == 0)
									val = "<a href='" + val + "'>"+ val + "</a>";
								
								str += "<i>" + key + "</i> :";
								str += val + "<br>";
							}

						}
						str += "<span>"
						str += "<hr>"

						str += obj["abstract"].value;

						if (strImage)
							$("#imagePanel").html(strImage);
						var oldHtml = $("#infoPanel").html();
						$("#infoPanel").html(oldHtml + str);

					}

				},
				error : function(xhr, err, msg) {
					console.log(xhr);
					console.log(err);
					console.log(msg);
				//	$("#imagePanel").html(xhr + err + msg);

				}
			});

}

function htmlEncode(value) {
	// create a in-memory div, set it's inner text(which jQuery
	// automatically encodes)
	// then grab the encoded contents back out. The div never exists on the
	// page.
	return $('<div/>').text(value).html();
}
